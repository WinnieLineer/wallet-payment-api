package com.example.wallet.di

import com.example.wallet.services.ReconciliationService
import com.example.wallet.services.TransactionService
import com.example.wallet.services.WalletService
import org.koin.dsl.module

val appModule = module {
    single { WalletService() }
    single { TransactionService(get()) }
    single { ReconciliationService(get()) }
}