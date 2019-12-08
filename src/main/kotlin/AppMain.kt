package net.obfuscatism.binview.gui

import javafx.application.Application
import javafx.stage.Stage

class AppMain : Application() {
    override fun start(primaryStage: Stage) {
        primaryStage.title = "Title"
        primaryStage.width = 300.0
        primaryStage.height = 200.0
        primaryStage.show()
    }
}
