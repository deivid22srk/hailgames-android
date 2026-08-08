package com.hailgames.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hailgames.app.data.AuthRepository
import com.hailgames.app.data.ContentRepository
import com.hailgames.app.data.StorageRepository

fun rememberSessionViewModel(): ViewModelProvider.Factory = viewModelFactory {
    initializer {
        SessionViewModel(AuthRepository())
    }
}

fun authViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
    initializer {
        AuthScreenViewModel(AuthRepository())
    }
}

fun homeViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
    initializer {
        HomeViewModel(ContentRepository())
    }
}

fun detailViewModelFactory(itemId: String): ViewModelProvider.Factory = viewModelFactory {
    initializer {
        ContentDetailViewModel(itemId, ContentRepository())
    }
}

fun adminPanelViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
    initializer {
        AdminPanelViewModel(ContentRepository())
    }
}

fun adminFormViewModelFactory(itemId: String?): ViewModelProvider.Factory = viewModelFactory {
    initializer {
        AdminFormViewModel(itemId, ContentRepository(), StorageRepository())
    }
}

fun adminManageViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
    initializer {
        AdminManageViewModel(AuthRepository())
    }
}
