package com.mercadopago.android.px.internal.callbacks;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.mercadopago.android.px.internal.repository.CongratsRepository;
import com.mercadopago.android.px.internal.repository.DisabledPaymentMethodRepository;
import com.mercadopago.android.px.internal.repository.EscPaymentManager;
import com.mercadopago.android.px.internal.repository.InstructionsRepository;
import com.mercadopago.android.px.internal.repository.PaymentRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.internal.viewmodel.PaymentModel;
import com.mercadopago.android.px.model.BusinessPayment;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.IPayment;
import com.mercadopago.android.px.model.IPaymentDescriptor;
import com.mercadopago.android.px.model.IPaymentDescriptorHandler;
import com.mercadopago.android.px.model.Instruction;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.PaymentTypes;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.services.Callback;
import com.mercadopago.android.px.tracking.internal.model.Reason;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import kotlin.Pair;
import kotlin.Unit;

public final class PaymentServiceHandlerWrapper implements PaymentServiceHandler {

    //TODO Remove handler when all views use PayButton or LiveData
    @Nullable private WeakReference<PaymentServiceHandler> handler;
    private PaymentServiceEventHandler eventHandler;
    @NonNull private final EscPaymentManager escPaymentManager;
    @NonNull private final InstructionsRepository instructionsRepository;
    @NonNull private final CongratsRepository congratsRepository;
    private UserSelectionRepository userSelectionRepository;
    @NonNull private final Queue<Message> messages;
    @NonNull /* default */ final PaymentRepository paymentRepository;
    @NonNull /* default */ final DisabledPaymentMethodRepository disabledPaymentMethodRepository;

    @NonNull private final IPaymentDescriptorHandler paymentHandler = new IPaymentDescriptorHandler() {
        @Override
        public void visit(@NonNull final IPaymentDescriptor payment) {
            final boolean shouldRecoverEsc = verifyAndHandleEsc(payment);

            if (shouldRecoverEsc) {
                onRecoverPaymentEscInvalid(paymentRepository.createRecoveryForInvalidESC());
            } else {
                paymentRepository.storePayment(payment);
                //Must be after store
                final PaymentResult paymentResult = paymentRepository.createPaymentResult(payment);
                disabledPaymentMethodRepository.handleDisableablePayment(paymentResult);
                if (paymentResult.isOffPayment()) {
                    instructionsRepository.getInstructions(paymentResult)
                        .enqueue(new Callback<List<Instruction>>() {
                            @Override
                            public void success(final List<Instruction> instructions) {
                                onPostPayment(payment, paymentResult);
                            }

                            @Override
                            public void failure(final ApiException apiException) {
                                onPostPayment(payment, paymentResult);
                            }
                        });
                } else {
                    onPostPayment(payment, paymentResult);
                }
            }
        }

        @Override
        public void visit(@NonNull final BusinessPayment businessPayment) {
            verifyAndHandleEsc(businessPayment);
            paymentRepository.storePayment(businessPayment);
            final PaymentResult paymentResult = paymentRepository.createPaymentResult(businessPayment);
            disabledPaymentMethodRepository.handleDisableablePayment(paymentResult);
            onPostPayment(businessPayment, paymentResult);
        }
    };

    public PaymentServiceHandlerWrapper(
        @NonNull final PaymentRepository paymentRepository,
        @NonNull final DisabledPaymentMethodRepository disabledPaymentMethodRepository,
        @NonNull final EscPaymentManager escPaymentManager,
        @NonNull final InstructionsRepository instructionsRepository,
        @NonNull final CongratsRepository congratsRepository,
        @NonNull final UserSelectionRepository userSelectionRepository) {
        this.paymentRepository = paymentRepository;
        this.disabledPaymentMethodRepository = disabledPaymentMethodRepository;
        this.escPaymentManager = escPaymentManager;
        this.instructionsRepository = instructionsRepository;
        this.congratsRepository = congratsRepository;
        this.userSelectionRepository = userSelectionRepository;
        messages = new LinkedList<>();
    }

    public PaymentServiceEventHandler getObservableEvents() {
        return eventHandler;
    }

    public void createTransactionLiveData() {
        eventHandler = new PaymentServiceEventHandler();
    }

    public void setHandler(@Nullable final PaymentServiceHandler handler) {
        this.handler = new WeakReference<>(handler);
    }

    public void detach(@Nullable final PaymentServiceHandler handler) {
        if (handler != null && this.handler != null && this.handler.get() != null &&
            this.handler.get().hashCode() == handler.hashCode()) {
            this.handler = null;
        }
    }

    @Override
    public void onCvvRequired(@NonNull final Card card, @NonNull final Reason reason) {
        addAndProcess(new CVVRequiredMessage(card, reason));
    }

    @Override
    public void onVisualPayment() {
        addAndProcess(new VisualPaymentMessage());
    }

    @Override
    public void onRecoverPaymentEscInvalid(final PaymentRecovery recovery) {
        addAndProcess(new RecoverPaymentEscInvalidMessage(recovery));
    }

    private boolean verifyAndHandleEsc(@NonNull final IPaymentDescriptor genericPayment) {
        boolean shouldRecoverEsc = false;
        final String paymentTypeId = userSelectionRepository.getPaymentMethod().getPaymentTypeId();
        if (paymentTypeId == null || PaymentTypes.isCardPaymentType(paymentTypeId)) {
            shouldRecoverEsc = handleEsc(genericPayment);
        }
        return shouldRecoverEsc;
    }

    @Override
    public void onPaymentFinished(@NonNull final IPaymentDescriptor payment) {
        // TODO remove - v5 when paymentTypeId is mandatory for payments
        payment.process(getHandler());
    }

    private void onPostPayment(@NonNull final IPaymentDescriptor payment, @NonNull final PaymentResult paymentResult) {
        congratsRepository.getPostPaymentData(payment, paymentResult, this::onPostPayment);
    }

    @Override
    public void onPostPayment(@NonNull final PaymentModel paymentModel) {
        addAndProcess(new PostPaymentMessage(paymentModel));
    }

    /* default */
    @VisibleForTesting
    @NonNull
    IPaymentDescriptorHandler getHandler() {
        return paymentHandler;
    }

    @Override
    public void onPaymentError(@NonNull final MercadoPagoError error) {
        if (handleEsc(error)) {
            // TODO we should not have this error anymore with cap check backend side.
            onRecoverPaymentEscInvalid(paymentRepository.createRecoveryForInvalidESC());
        } else {
            addAndProcess(new ErrorMessage(error));
        }
    }

    private boolean handleEsc(@NonNull final MercadoPagoError error) {
        return escPaymentManager.manageEscForError(error, paymentRepository.getPaymentDataList());
    }

    private boolean handleEsc(@NonNull final IPayment payment) {
        return escPaymentManager.manageEscForPayment(paymentRepository.getPaymentDataList(),
            payment.getPaymentStatus(),
            payment.getPaymentStatusDetail());
    }

    /* default */ void addAndProcess(@NonNull final Message message) {
        messages.add(message);
        processMessages();
    }

    public void processMessages() {
        final PaymentServiceHandler currentHandler = handler != null ? handler.get() : null;
        while (!messages.isEmpty()) {
            final Message polledMessage = messages.poll();
            polledMessage.processMessage(currentHandler, eventHandler);
        }
    }

    //region messages

    private interface Message {
        void processMessage(@Nullable final PaymentServiceHandler handler,
            @NonNull final PaymentServiceEventHandler eventHandler);
    }

    private static class CVVRequiredMessage implements Message {

        @NonNull private final Card card;
        @NonNull private final Reason reason;

        /* default */ CVVRequiredMessage(@NonNull final Card card, @NonNull final Reason reason) {
            this.card = card;
            this.reason = reason;
        }

        @Override
        public void processMessage(@Nullable final PaymentServiceHandler handler,
            @Nullable final PaymentServiceEventHandler eventHandler) {
            if (handler != null) {
                handler.onCvvRequired(card, reason);
            }
            if(eventHandler != null) {
                eventHandler.getRequireCvvLiveData().setValue(new Pair<>(card, reason));
            }
        }
    }

    private static class RecoverPaymentEscInvalidMessage implements Message {

        private final PaymentRecovery recovery;

        /* default */ RecoverPaymentEscInvalidMessage(final PaymentRecovery recovery) {
            this.recovery = recovery;
        }

        @Override
        public void processMessage(@Nullable final PaymentServiceHandler handler,
            @Nullable final PaymentServiceEventHandler eventHandler) {
            if (handler != null) {
                handler.onRecoverPaymentEscInvalid(recovery);
            }
            if(eventHandler != null) {
                eventHandler.getRecoverInvalidEscLiveData().setValue(recovery);
            }
        }
    }

    private static class PostPaymentMessage implements Message {

        @NonNull private final PaymentModel paymentModel;

        /* default */ PostPaymentMessage(@NonNull final PaymentModel paymentModel) {
            this.paymentModel = paymentModel;
        }

        @Override
        public void processMessage(@Nullable final PaymentServiceHandler handler,
            @Nullable final PaymentServiceEventHandler eventHandler) {
            if (handler != null) {
                handler.onPostPayment(paymentModel);
            }
            if(eventHandler != null) {
                eventHandler.getPaymentFinishedLiveData().setValue(paymentModel);
            }
        }
    }

    private static class ErrorMessage implements Message {

        @NonNull private final MercadoPagoError error;

        /* default */ ErrorMessage(@NonNull final MercadoPagoError error) {
            this.error = error;
        }

        @Override
        public void processMessage(@Nullable final PaymentServiceHandler handler,
            @Nullable final PaymentServiceEventHandler eventHandler) {
            if (handler != null) {
                handler.onPaymentError(error);
            }
            if(eventHandler != null) {
                eventHandler.getPaymentErrorLiveData().setValue((error));
            }
        }
    }

    private static class VisualPaymentMessage implements Message {
        @Override
        public void processMessage(@Nullable final PaymentServiceHandler handler,
            @Nullable final PaymentServiceEventHandler eventHandler) {
            if (handler != null) {
                handler.onVisualPayment();
            }
            if(eventHandler != null) {
                eventHandler.getVisualPaymentLiveData().setValue(Unit.INSTANCE);
            }
        }
    }

    //endregion
}