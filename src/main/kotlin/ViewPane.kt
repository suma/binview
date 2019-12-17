package net.obfuscatism.binview.gui

import javafx.concurrent.Service
import javafx.concurrent.Task
import javafx.concurrent.WorkerStateEvent
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.layout.Pane
import org.msgpack.core.MessagePack
import java.lang.Thread.sleep
import java.util.concurrent.ConcurrentLinkedQueue

class ViewPane : Pane {
    var filePath: String? = null
    private var service: BackendProcessService? = null
    private var backendThread: BackendThread? = null
    private var backendRequest: ConcurrentLinkedQueue<RPCRequest> = ConcurrentLinkedQueue()
    private var backendResponse: ConcurrentLinkedQueue<RPCResponse> = ConcurrentLinkedQueue()

    constructor(path: String) : super() {
        filePath = path

        backendThread = BackendThread(path, backendRequest, backendResponse)
        backendThread!!.isDaemon = true
        backendThread!!.start()

        service = BackendProcessService()
        service!!.filePath = path
        service!!.onSucceeded = EventHandler<WorkerStateEvent> {
            fun handle(ev: ActionEvent) {
                println("done:" + ev.source)
                service!!.restart()
            }
        }
    }

    fun shutdown() {
        backendThread!!.shutdown()
    }
}

class BackendThread : Thread {
    var filePath: String? = null
    var request: ConcurrentLinkedQueue<RPCRequest>? = null
    var response: ConcurrentLinkedQueue<RPCResponse>? = null
    var backendProcess: Process? = null
    var shutdown: Boolean = false

    constructor(
        path: String,
        backendRequest: ConcurrentLinkedQueue<RPCRequest>,
        backendResponse: ConcurrentLinkedQueue<RPCResponse>
    ) {
        filePath = path
        request = backendRequest
        response = backendResponse
    }

    fun shutdown() {
        shutdown = true

        // kill process immediately
        backendProcess!!.destroy()
    }

    override fun run() {
        backendProcess = ProcessBuilder(getBackendPath(), filePath).start()
        val output = backendProcess!!.inputStream
        val unpacker = MessagePack.newDefaultUnpacker(output)

        while (!shutdown) {
            var processed = 0

            // request
            val req = request!!.poll()
            if (req != null) {
                // Write request to backend process
                //println(req)
                processed += 1
            }

            // response
            if (unpacker.hasNext()) {
                val v = unpacker.unpackValue()
                // TODO: convert object to RPCResponse
                //response!!.add(req)
                processed += 1
            }

            if (processed == 0) {
                // idle
                sleep(50)
            }
        }
    }
}

enum class RPCResponseType {
    Update,
}

class RPCResponse {
    var requestId: Int = 0
}

class RPCRequest {
    var requestId: Int = 0
}

class BackendProcessService : Service<RPCResponse>() {
    var filePath: String? = null
    var backendResponse: ConcurrentLinkedQueue<RPCResponse>? = null

    override fun restart() {
        sleep(50)
    }

    override fun createTask(): Task<RPCResponse> {
        return object : Task<RPCResponse>() {
            override fun call(): RPCResponse? {
                val res = backendResponse!!.poll()
                if (res != null) {
                    return res
                }
                return null
            }
        }
    }
}