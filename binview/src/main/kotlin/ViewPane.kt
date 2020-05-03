package net.obfuscatism.binview.gui

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import javafx.application.Platform
import javafx.concurrent.Service
import javafx.concurrent.Task
import javafx.concurrent.WorkerStateEvent
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.layout.Pane
import org.msgpack.core.MessagePack
import org.msgpack.jackson.dataformat.MessagePackFactory
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

        backendThread = BackendThread(this, path, backendRequest, backendResponse)
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

    fun flushQueue() {
        while (backendResponse.size > 0) {
            val res: RPCResponse? = backendResponse!!.poll() ?: break
            println(res)    // DEBUG
            Platform.runLater(object : Runnable {
                    override fun run() {
                        if (res != null) {
                            updateView(res)
                        }
                    }
                }
            )
        }
    }

    private fun updateView(res: RPCResponse) {
    }
}

data class Color(val color: String, val kana: String)
class BackendThread : Thread {
    var viewPane: ViewPane? = null
    var filePath: String? = null
    var request: ConcurrentLinkedQueue<RPCRequest>? = null
    var response: ConcurrentLinkedQueue<RPCResponse>? = null
    var backendProcess: Process? = null
    var shutdown: Boolean = false
    private var requestId: Long = 0

    constructor(
        view: ViewPane,
        path: String,
        backendRequest: ConcurrentLinkedQueue<RPCRequest>,
        backendResponse: ConcurrentLinkedQueue<RPCResponse>
    ) {
        viewPane = view
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
        val input = backendProcess!!.inputStream
        val output = backendProcess!!.outputStream
        //val packer = MessagePack.newDefaultPacker(output)
        val unpacker = MessagePack.newDefaultUnpacker(input)

        var addReq = RPCRequest()
        addReq.params.plus(Pair("key", "value"))
        request!!.add(addReq)

        val factory = MessagePackFactory()
        factory.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
        factory.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE)
        var mapper: ObjectMapper = ObjectMapper(factory)
        while (!shutdown) {
            var processed = 0

            // request
            val req = request!!.poll()
            if (req != null) {
                // Write request to backend process
                mapper.writeValue(output, req)
                processed += 1
                //println("Req: " + requestId + " processed: " + processed)
            }

            // response
            try {
                if (unpacker.hasNext()) {
                    val v = mapper.readValue(input, RPCResponse::class.java)
                    response!!.add(v)
                    processed += 1
                }

                viewPane!!.flushQueue()
            } catch (e: Exception) {
                println("unpacked failed")
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

@JsonFormat(shape=JsonFormat.Shape.OBJECT)
data class RPCResponse(val requestId: Int, val value: HashMap<String, Object>)

@JsonFormat(shape=JsonFormat.Shape.OBJECT)
class RPCRequest {
    var reqId: Int = 0
    var params: Map<String, Object> = HashMap()
}

class BackendProcessService : Service<RPCResponse>() {
    var filePath: String? = null
    var backendResponse: ConcurrentLinkedQueue<RPCResponse> = ConcurrentLinkedQueue()

    override fun restart() {
        sleep(50)
    }

    override fun createTask(): Task<RPCResponse> {
        return object : Task<RPCResponse>() {
            override fun call(): RPCResponse? {
                val res = backendResponse.poll()
                if (res != null) {
                    return res
                }
                return null
            }
        }
    }
}