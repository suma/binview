package net.obfuscatism.binview.gui

import java.io.File
import java.nio.file.Paths

fun getBackendPath(): String {
    // Debug build
    val debug = System.getenv("DEBUG")
    println(debug)
    if (debug != null && !debug.isEmpty()) {
        val path = System.getProperty("user.dir")
        val base = File(path).getParent().toString()
        return Paths.get(base, "backend", "build", "rust-project", "target", "release", "backend").toString()
    }

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
