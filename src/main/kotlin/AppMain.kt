package net.obfuscatism.binview.gui

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.Tab
import javafx.scene.layout.BorderPane
import javafx.stage.Stage

class AppMain : Application() {
    private var tabView: TabView? = null

    override fun start(primaryStage: Stage) {
        val borderPane: BorderPane  = BorderPane()
        tabView = createTabView()
        borderPane.top = createMenu()
        borderPane.center = tabView

        primaryStage.scene = Scene(borderPane);

        primaryStage.title = "Title"
        primaryStage.width = 300.0
        primaryStage.height = 200.0
        primaryStage.show()
    }

    private fun createMenu(): MenuBar {
        val menuBar: MenuBar = MenuBar()

        val menuFile = Menu("File")
        val menuOpen = MenuItem("Open")
        val menuExit = MenuItem("Close")
        menuFile.items.addAll(menuOpen, menuExit)

        menuBar.menus.addAll(menuFile)

        val os = System.getProperty("os.name")
        if (os != null && os.startsWith("Mac")) {
            menuBar.useSystemMenuBarProperty().set(true)
        }
        return menuBar
    }

    private fun createTabView(): TabView {
        val tabView = TabView()

        val tabDummy = Tab()
        tabDummy.setText("tab1")
        tabDummy.content = javafx.scene.control.Label("test")
        tabView.tabs.addAll(tabDummy)

        return tabView
    }
}
