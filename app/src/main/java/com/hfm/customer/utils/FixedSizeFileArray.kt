package com.hfm.customer.utils

class FixedSizeFileArray(private val maxSize: Int) {
    private val files = Array<String?>(maxSize) { null }
    private var currentIndex = 0

    fun addFile(file: String) {
        files[currentIndex] = file
        currentIndex = (currentIndex + 1) % maxSize
    }

    fun getFiles(): List<String> {
        return files.filterNotNull()
    }
}
