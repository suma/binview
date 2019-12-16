package net.obfuscatism.binview.gui

import java.io.File

fun getBackendPath(): String {
    val os = System.getProperty("os.name")
    if (os != null && os.startsWith("Mac")) {
        return getMacBinPath()
    } else {
        throw RuntimeException("not implemented on your OS")
    }
    // TODO: Linux, Windows
}

fun getMacBinPath(): String {
    return File(AppMain ().javaClass.protectionDomain.codeSource.location.path).parentFile.parent.toString() + "/MacOS/backend"
}
