package com.example.handispace.utils

data class ResultState<DataType>(
    var isLoading: Boolean = false,
    var data: DataType? = null,
    var errorMessage: String = ""
)