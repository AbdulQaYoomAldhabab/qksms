package com.moez.QKSMS.presentation.settings

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.presentation.base.QkActivity
import com.moez.QKSMS.presentation.view.PreferenceView
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.settings_activity.*

class SettingsActivity : QkActivity<SettingsViewModel>(), SettingsView {

    override val viewModelClass = SettingsViewModel::class
    override val preferenceClickIntent: Subject<PreferenceView> = PublishSubject.create()
    override val themeSelectedIntent by lazy { themeAdapter.colorSelected }

    private val progressDialog by lazy {
        // TODO remove this
        ProgressDialog(this).apply {
            isIndeterminate = true
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }
    }

    private val themeDialog by lazy {
        AlertDialog.Builder(this)
                .setTitle(R.string.settings_dialog_theme_title)
                .setView(themeRecyclerView)
                .setCancelable(false)
                .create()
    }

    private val themeRecyclerView by lazy {
        RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@SettingsActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = themeAdapter
        }
    }

    private val themeAdapter by lazy {
        ThemeAdapter(this@SettingsActivity).apply {
            data = listOf(0xFF008389, 0xFF04CBC9, 0xFF00D99B, 0xFF00A3F3).map { it.toInt() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppComponentManager.appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        setTitle(R.string.title_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel.bindView(this)

        // Listen to clicks for all of the preferences
        (0 until preferences.childCount)
                .map { index -> preferences.getChildAt(index) }
                .filter { view -> view is PreferenceView }
                .forEach { preference ->
                    preference.clicks().subscribe {
                        preferenceClickIntent.onNext(preference as PreferenceView)
                    }
                }
    }

    override fun render(state: SettingsState) {
        if (progressDialog.isShowing && !state.syncing) progressDialog.dismiss()
        else if (!progressDialog.isShowing && state.syncing) progressDialog.show()

        if (themeDialog.isShowing && !state.selectingTheme) themeDialog.dismiss()
        else if (!themeDialog.isShowing && state.selectingTheme) themeDialog.show()

        defaultSms.summary = getString(when (state.isDefaultSmsApp) {
            true -> R.string.settings_default_sms_summary_true
            else -> R.string.settings_default_sms_summary_false
        })
    }

}