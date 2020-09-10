package com.mercadopago.android.px.internal.features.pay_button

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.mercadolibre.android.andesui.snackbar.AndesSnackbar
import com.mercadolibre.android.andesui.snackbar.duration.AndesSnackbarDuration
import com.mercadolibre.android.andesui.snackbar.type.AndesSnackbarType
import com.mercadolibre.android.ui.widgets.MeliButton
import com.mercadopago.android.px.R
import com.mercadopago.android.px.addons.BehaviourProvider
import com.mercadopago.android.px.addons.internal.SecurityValidationHandler
import com.mercadopago.android.px.addons.model.SecurityValidationData
import com.mercadopago.android.px.internal.di.Session
import com.mercadopago.android.px.internal.extensions.orIfEmpty
import com.mercadopago.android.px.internal.features.Constants
import com.mercadopago.android.px.internal.features.SecurityCodeActivity
import com.mercadopago.android.px.internal.features.dummy_result.DummyResultActivity
import com.mercadopago.android.px.internal.features.explode.ExplodeDecorator
import com.mercadopago.android.px.internal.features.explode.ExplodingFragment
import com.mercadopago.android.px.internal.features.payment_congrats.PaymentCongrats
import com.mercadopago.android.px.internal.features.payment_congrats.model.PaymentCongratsModelMapper
import com.mercadopago.android.px.internal.features.payment_result.PaymentResultActivity
import com.mercadopago.android.px.internal.features.plugins.PaymentProcessorActivity
import com.mercadopago.android.px.internal.util.FragmentUtil
import com.mercadopago.android.px.internal.util.ViewUtils
import com.mercadopago.android.px.internal.view.OnSingleClickListener
import com.mercadopago.android.px.internal.viewmodel.PostPaymentAction
import com.mercadopago.android.px.model.Card
import com.mercadopago.android.px.model.PaymentRecovery
import com.mercadopago.android.px.tracking.internal.events.FrictionEventTracker
import com.mercadopago.android.px.tracking.internal.model.Reason
import com.mercadopago.android.px.internal.viewmodel.PayButtonViewModel as ButtonConfig

class PayButtonFragment : Fragment(), PayButton.View, SecurityValidationHandler {

    private var buttonStatus = MeliButton.State.NORMAL
    private lateinit var button: MeliButton
    private lateinit var viewModel: PayButtonViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pay_button, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = Session.getInstance().viewModelModule.get(this, PayButtonViewModel::class.java)

        when {
            targetFragment is PayButton.Handler -> viewModel.attach(targetFragment as PayButton.Handler)
            parentFragment is PayButton.Handler -> viewModel.attach(parentFragment as PayButton.Handler)
            context is PayButton.Handler -> viewModel.attach(context as PayButton.Handler)
            else -> throw IllegalStateException("Parent should implement ${PayButton.Handler::class.java.simpleName}")
        }

        button = view.findViewById(R.id.confirm_button)
        button.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                viewModel.preparePayment()
            }
        })
        savedInstanceState?.let {
            buttonStatus = it.getInt(EXTRA_STATE, MeliButton.State.NORMAL)
            button.visibility = it.getInt(EXTRA_VISIBILITY, VISIBLE)
            viewModel.recoverFromBundle(it)
        }
        updateButtonState()

        with(viewModel) {
            buttonTextLiveData.observe(viewLifecycleOwner,
                    Observer { buttonConfig -> button.text = buttonConfig!!.getButtonText(this@PayButtonFragment.context!!) })
            cvvRequiredLiveData.observe(viewLifecycleOwner,
                    Observer { pair -> pair?.let { showSecurityCodeScreen(it.first, it.second) } })
            recoverRequiredLiveData.observe(viewLifecycleOwner,
                    Observer { recovery -> recovery?.let { showSecurityCodeForRecovery(it) } })
            stateUILiveData.observe(viewLifecycleOwner, Observer { state -> state?.let { onStateUIChanged(it) } })
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(EXTRA_STATE, buttonStatus)
        outState.putInt(EXTRA_VISIBILITY, button.visibility)
        viewModel.storeInBundle(outState)
    }

    private fun onStateUIChanged(stateUI: PayButtonState) {
        when (stateUI) {
            is UIProgress.FingerprintRequired -> startBiometricsValidation(stateUI.validationData)
            is UIProgress.ButtonLoadingStarted -> startLoadingButton(stateUI.timeOut, stateUI.buttonConfig)
            is UIProgress.ButtonLoadingFinished -> finishLoading(stateUI.explodeDecorator)
            is UIProgress.ButtonLoadingCanceled -> cancelLoading()
            is UIResult.VisualProcessorResult -> PaymentProcessorActivity.start(this, REQ_CODE_PAYMENT_PROCESSOR)
            is UIError.ConnectionError -> showSnackBar(stateUI.message)
            is UIResult.PaymentResult -> PaymentResultActivity.start(this, REQ_CODE_CONGRATS, stateUI.model)
            is UIResult.NoCongratsResult -> DummyResultActivity.start(this, REQ_CODE_CONGRATS, stateUI.model)
            is UIResult.CongratsPaymentModel -> PaymentCongrats.show(stateUI.model, activity, REQ_CODE_CONGRATS)
        }
    }

    override fun stimulate() {
        viewModel.preparePayment()
    }

    override fun enable() {
        buttonStatus = MeliButton.State.NORMAL
        updateButtonState()
    }

    override fun disable() {
        buttonStatus = MeliButton.State.DISABLED
        updateButtonState()
    }

    private fun updateButtonState() {
        if (::button.isInitialized) {
            button.state = buttonStatus
        }
    }

    @SuppressLint("Range")
    private fun showSnackBar(error: String) {
        view?.let {
            it.context?.let { context ->
                AndesSnackbar(context, it, AndesSnackbarType.ERROR, error.orIfEmpty(context.getString(R.string.px_error_title)), AndesSnackbarDuration.LONG).show()
            }
        }
    }

    private fun startBiometricsValidation(validationData: SecurityValidationData) {
        disable()
        BehaviourProvider.getSecurityBehaviour().startValidation(this, validationData, REQ_CODE_BIOMETRICS)
    }

    override fun onAnimationFinished() {
        viewModel.hasFinishPaymentAnimation()
    }

    override fun onSecurityValidated(isSuccess: Boolean, securityValidated: Boolean) {
        viewModel.handleBiometricsResult(isSuccess, securityValidated)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQ_CODE_BIOMETRICS) {
            val securityRequested = data?.getBooleanExtra(
                    BehaviourProvider.getSecurityBehaviour().extraResultKey, false) ?: false
            enable()
            onSecurityValidated(resultCode == Activity.RESULT_OK, securityRequested)
        } else if (requestCode == REQ_CODE_SECURITY_CODE) {
            cancelLoading()
            if (resultCode == Activity.RESULT_OK) {
                viewModel.startPayment()
            } else {
                enable()
            }
        } else if (requestCode == REQ_CODE_CONGRATS && resultCode == Constants.RESULT_ACTION) {
            handleAction(data)
        } else if (resultCode == Constants.RESULT_PAYMENT) {
            viewModel.onPostPayment(PaymentProcessorActivity.getPaymentModel(data))
        } else if (resultCode == Constants.RESULT_FAIL_ESC) {
            viewModel.onRecoverPaymentEscInvalid(PaymentProcessorActivity.getPaymentRecovery(data)!!)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleAction(data: Intent?) {
        data?.extras?.let { viewModel.onPostPaymentAction(PostPaymentAction.fromBundle(it)) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.detach()
    }

    override fun onDestroy() {
        FragmentUtil.tryRemoveNow(childFragmentManager, ExplodingFragment.TAG)
        super.onDestroy()
    }

    private fun finishLoading(params: ExplodeDecorator) {
        childFragmentManager.findFragmentByTag(ExplodingFragment.TAG)
                ?.let { (it as ExplodingFragment).finishLoading(params) }
                ?: viewModel.hasFinishPaymentAnimation()
    }

    private fun startLoadingButton(paymentTimeout: Int, buttonConfig: ButtonConfig) {
        context?.let {
            button.post {
                if (!isAdded) {
                    FrictionEventTracker.with("/px_checkout/pay_button_loading", FrictionEventTracker.Id.GENERIC,
                            FrictionEventTracker.Style.SCREEN, emptyMap<String, String>())
                } else {
                    val explodingFragment = ExplodingFragment.newInstance(
                        buttonConfig.getButtonProgressText(it), paymentTimeout)
                    childFragmentManager.beginTransaction()
                            .add(R.id.exploding_frame, explodingFragment, ExplodingFragment.TAG)
                            .commitNowAllowingStateLoss()
                    hideConfirmButton()
                }
            }
        }
    }

    private fun cancelLoading() {
        showConfirmButton()
        val fragment = childFragmentManager.findFragmentByTag(ExplodingFragment.TAG) as ExplodingFragment?
        if (fragment != null && fragment.isAdded && fragment.hasFinished()) {
            childFragmentManager
                    .beginTransaction()
                    .remove(fragment)
                    .commitNowAllowingStateLoss()
            restoreStatusBar()
        }
    }

    private fun restoreStatusBar() {
        activity?.let {
            ViewUtils.setStatusBarColor(ContextCompat.getColor(it, R.color.px_colorPrimaryDark), it.window)
        }
    }

    private fun hideConfirmButton() {
        button.clearAnimation()
        button.visibility = INVISIBLE
    }

    private fun showConfirmButton() {
        button.clearAnimation()
        button.visibility = VISIBLE
    }

    private fun showSecurityCodeForRecovery(recovery: PaymentRecovery) {
        cancelLoading()
        SecurityCodeActivity.startForRecovery(this, recovery, REQ_CODE_SECURITY_CODE)
    }

    private fun showSecurityCodeScreen(card: Card, reason: Reason?) {
        SecurityCodeActivity.startForSavedCard(this, card, reason, REQ_CODE_SECURITY_CODE)
    }

    override fun isExploding(): Boolean {
        return FragmentUtil.isFragmentVisible(childFragmentManager, ExplodingFragment.TAG)
    }

    override fun getParentView() = button

    companion object {
        const val TAG = "TAG_BUTTON_FRAGMENT"
        const val REQ_CODE_CONGRATS = 300
        private const val REQ_CODE_SECURITY_CODE = 301
        private const val REQ_CODE_PAYMENT_PROCESSOR = 302
        private const val REQ_CODE_BIOMETRICS = 303
        private const val EXTRA_STATE = "extra_state"
        private const val EXTRA_VISIBILITY = "extra_visibility"
    }
}