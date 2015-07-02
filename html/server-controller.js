var serverUrl = null;
var port = "8001";
var messagesSets = [];

function scanForServer(f) {

    boards = new Array();
    serverUrl = "";

    var detail = document.getElementById("detailPanel");
    detail.style.visibility = "hidden";

    var panel = document.getElementById("ipAddress");
    while (panel.firstChild) {
        panel.removeChild(panel.firstChild);
    }
    var p = document.createElement("H4");
    var t = document.createTextNode("Searching for Server...");
    p.appendChild(t);
    panel.appendChild(p);

    for ( var i=0; i<=255; i++ ) {
        pingServer(i, f);
    }

}

function getServerBoardStatus() {
    var p = document.getElementById("boardResponses");
    while ( p.firstChild ) {
        p.removeChild(p.firstChild);
    }
    p.style.visibility = "hidden";
    var req = new XMLHttpRequest();
    req.onreadystatechange = function() {
        if ( req.readyState==4 ) {
            if ( req.status!=200 ) {
                window.alert("Error " + req.status + ": " + req.responseText);
            } else {
                var boardResponses = document.getElementById("boardResponses");
                boardResponses.style.visibility = "visible";
                var json = JSON.parse(req.responseText);
                var lbs = json.boards;
                if ( lbs.length==0 ) {
                    var t = document.createTextNode("No LightBoards are registered with the Ticket Server.");
                    boardResponses.appendChild(t);
                }
                for ( var i=0; i<lbs.length; i++ ) {
                    var lb = lbs[i];
                    var div = document.createElement("DIV");
                    div.style.float = "left";
                    div.style.clear = "left";
                    if ( lb.status==="OK" ) {
                        div.style.color = "#00AA00";
                    } else {
                        div.style.color = "red";
                    }
                    var t = document.createTextNode(lb.ip + ": " + lb.message);
                    div.appendChild(t);
                    boardResponses.appendChild(div);
                }
            }
        } else
        if (req.readyState > 1 && req.status != 200 && req.status != 304) {
            window.alert("Server responded with an error - please contact Barri.");
        }
    }
    req.open("GET", "http://"+serverUrl+":8002/ticketserver/lightboards", true);
    req.send();
}

function pingServer(ipLSB, f) {

    var req = new XMLHttpRequest();
    req.onreadystatechange = function() {
        if ( req.readyState==4 ) {
            if ( req.status==200 ) {
                serverUrl = "192.168.0."+ipLSB;
                var panel = document.getElementById("ipAddress");
                while (panel.firstChild) {
                    panel.removeChild(panel.firstChild);
                }
                var p = document.createElement("H3");
                var t = document.createTextNode("Server IP: " + serverUrl);
                p.appendChild(t);
                panel.appendChild(p);
                var detail = document.getElementById("detailPanel");
                detail.style.visibility = "visible";
                f();
            } else {
                if ( ipLSB==255 && serverUrl=="" ) {
                    var panel = document.getElementById("ipAddress");
                    while (panel.firstChild) {
                        panel.removeChild(panel.firstChild);
                    }
                    var p = document.createElement("H3");
                    var t = document.createTextNode("Cannot Find Server");
                    p.appendChild(t);
                    panel.appendChild(p);
                }
            }
        }
    }
    req.open("GET", "http://192.168.0."+ipLSB+":8002/ticketserver/hello", true);
    req.send();

}

function clearRegistrations() {
    var req = new XMLHttpRequest();
    req.onreadystatechange = function() {
        if ( req.readyState==4 ) {
            if ( req.status==200 ) {
                scanForServer(function() { getServerBoardStatus(); });
                scanLightBoards();
            } else {
                window.alert("Error Clearing Registrations");
            }
        }
    }
    req.open("POST", "http://"+serverUrl+":8002/ticketserver/clear-registrations", true);
    req.send();
}

function refreshAll() {
    var req = new XMLHttpRequest();
    req.onreadystatechange = function() {
        if ( req.readyState==4 ) {
            if ( req.status==200 ) {
                scanForServer(function() { getServerBoardStatus(); });
                scanLightBoards();
            } else {
                window.alert("Error Updating Boards");
            }
        }
    }
    req.open("POST", "http://"+serverUrl+":8002/ticketserver/update-all", true);
    req.send();
}

function shutdownServer() {
    var req = new XMLHttpRequest();
    req.onreadystatechange = function() {
        if ( req.readyState==4 ) {
            if ( req.status==200 ) {
            setTimeout(function() {
                location.reload();
            },3000);
            } else {
                window.alert("Error Shutting Down Server");
            }
        }
    }
    req.open("POST", "http://"+serverUrl+":8002/ticketserver/shutdown", true);
    req.send();
}

function loadMessages() {
    messageSets = [];
    var req = new XMLHttpRequest();
    req.onreadystatechange = function() {
        if ( req.readyState==4 && req.status==200 ) {
            var json = JSON.parse(req.responseText);
            var bundles = json.bundles;
            var panel = document.getElementById("messageSets");
            while (panel.firstChild) {
                panel.removeChild(panel.firstChild);
            }
            for ( var i=0; i<bundles.length; i++ ) {
                var bundle = bundles[i];
                messageSets.push(bundle.name);

                var t = document.createTextNode(bundle.name);
                var h3 = document.createElement("H2");
                h3.appendChild(t);

                var div = document.createElement("DIV");
                div.id = "messageList"+bundle.name;

                panel.appendChild(h3);
                panel.appendChild(div);

                putMessages(bundle.id, bundle.name, bundle.messages);
            }
//            loadBroadcastMessage();
        }
    }
    req.open("GET", "http://"+serverUrl+":8002/ticketserver/messages", true);
    req.send();
}

function loadBroadcastMessage() {
    var req = new XMLHttpRequest();
    req.onreadystatechange = function() {
        if ( req.readyState==4 && req.status==200 ) {
            window.alert(req.responseText);
            var json = JSON.parse(req.responseText);
        }
    }
    req.open("GET", "http://"+serverUrl+":8002/broadcast", true);
    req.send();
}

function putMessages(id, name, messages) {
    var panel = document.getElementById("messageList"+name);
    panel.style.visibility = "visible";
    while (panel.firstChild) {
        panel.removeChild(panel.firstChild);
    }
    for ( var i in messages ) {
        var msg = messages[i];

        var div = document.createElement("DIV");
        div.style.width = "100%";
        div.style.border = "1px solid #AAAAAA";
        div.style.float = "left";
        div.style.margin = "5px";
        div.style.background = "white";
        div.style.boxShadow = "2px 2px 2px #AAAAAA";
        div.style.borderRadius = "5px";

        var p = document.createElement("INPUT");
        p.id = "input"+id+"-"+i;
        p.style.width = "85%";
        p.style.height = "40px";
        p.style.float = "left";
        p.style.fontSize = "16px";
        p.style.padding = "4px";
        p.style.border = "none";
        p.style.borderRadius = "5px";
        p.value = msg;
        p.onchange = createUpdateMessageFunction(id, i);

        var removeBtn = document.createElement("BUTTON");
        removeBtn.appendChild(document.createTextNode("Remove"));
        removeBtn.className = "btn-danger";
        removeBtn.style.float = "right";
        removeBtn.style.height = "40px";
        removeBtn.style.width = "15%";
        removeBtn.style.borderRadius = "0px 5px 5px 0px"
        removeBtn.onclick = createRemoveMessageFunction(id, i);

        div.appendChild(p);
        div.appendChild(removeBtn);

        panel.appendChild(div);

    }

    var addDiv = document.createElement("DIV");
    addDiv.style.width = "100%";
    addDiv.style.border = "1px solid #AAAAAA";
    addDiv.style.float = "left";
    addDiv.style.margin = "5px";
    addDiv.style.background = "white";
    addDiv.style.boxShadow = "2px 2px 2px #AAAAAA";
    addDiv.style.borderRadius = "5px";

    var addInput = document.createElement("INPUT");
    addInput.id = "addInput"+id;
    addInput.style.width = "85%";
    addInput.style.height = "40px";
    addInput.style.float = "left";
    addInput.style.fontSize = "16px";
    addInput.style.padding = "4px";
    addInput.style.border = "none";
    addInput.style.borderRadius = "5px";

    addInput.onchange = createAddMessageFunction(id);

    var addBtn = document.createElement("BUTTON");
    addBtn.appendChild(document.createTextNode("Add"));
    addBtn.className = "btn-success";
    addBtn.style.float = "right";
    addBtn.style.height = "40px";
    addBtn.style.width = "15%";
    addBtn.style.borderRadius = "0px 5px 5px 0px"

    addDiv.appendChild(addInput);
    addDiv.appendChild(addBtn);

    panel.appendChild(addDiv);

    var spacer = document.createElement("DIV");
    spacer.style.width = "100%";
    spacer.style.height = "40px";
    spacer.style.float = "left";
    spacer.style.margin = "5px";

    panel.appendChild(spacer);
}

function createAddMessageFunction(setId) {
    return function() {
        var field = document.getElementById("addInput"+setId);
        var text = field.value;
        if ( text!="" ) {
            var req = new XMLHttpRequest();
            req.onreadystatechange = function() {
                if ( req.readyState==4 ) {
                    if ( req.status!=200 ) {
                        window.alert("Error " + req.status + ": " + req.responseText);
                    } else {
                        loadMessages();
                    }
                } else
                if (req.readyState > 1 && req.status != 200 && req.status != 304) {
                    window.alert("Server responded with an error - please contact Barri.");
                }
            }
            req.open("POST", "http://"+serverUrl+":8002/ticketserver/add-message?setId="+setId+"&text="+text, true);
            req.send();
        }
    }
}

function createUpdateMessageFunction(setId, messageId) {
    return function() {
        var field = document.getElementById("input"+setId+"-"+messageId);
        var text = field.value;
        var req = new XMLHttpRequest();
        req.onreadystatechange = function() {
            if ( req.readyState==4 ) {
                if ( req.status!=200 ) {
                    window.alert("Error " + req.status + ": " + req.responseText);
                } else {
                    loadMessages();
                }
            } else
            if (req.readyState > 1 && req.status != 200 && req.status != 304) {
                window.alert("Server responded with an error - please contact Barri.");
            }
        }
        req.open("POST", "http://"+serverUrl+":8002/ticketserver/update-message?setId="+setId+"&msgId="+messageId+"&text="+text, true);
        req.send();
    }
}

function createRemoveMessageFunction(setId, messageId) {
    return function() {
        var req = new XMLHttpRequest();
        req.onreadystatechange = function() {
            if ( req.readyState==4 ) {
                if ( req.status!=200 ) {
                    window.alert("Error " + req.status + ": " + req.responseText);
                } else {
                    loadMessages();
                }
            } else
            if (req.readyState > 1 && req.status != 200 && req.status != 304) {
                window.alert("Server responded with an error - please contact Barri.");
            }
        }
        req.open("POST", "http://"+serverUrl+":8002/ticketserver/remove-message?setId="+setId+"&msgId="+messageId, true);
        req.send();
    }
}

function beginBroadcast() {
    var text = document.getElementById("broadcastMessageText").value;
    var interval = parseInt(document.getElementById("broadcastMessageInterval").value);
    if ( text!=="" ) {
        var url = "http://"+serverUrl+":8002/ticketserver/broadcast";
        if ( !isNaN(interval) ) {
            url += "?interval="+interval;
        }
        var req = new XMLHttpRequest();
        req.onreadystatechange = function() {
            if ( req.readyState==4 ) {
                if ( req.status!=200 ) {
                    window.alert("Error " + req.status + ": " + req.responseText);
                } else {
                    loadMessages();
                }
            } else
            if (req.readyState > 1 && req.status != 200 && req.status != 304) {
                window.alert("Server responded with an error - please contact Barri.");
            }
        }
        req.open("POST", url, true);
        req.send(text);
    }
}

function cancelBroadcast() {
    document.getElementById("broadcastMessageText").value = "";
    document.getElementById("broadcastMessageInterval").value = "";
    var req = new XMLHttpRequest();
        req.onreadystatechange = function() {
            if ( req.readyState==4 ) {
                if ( req.status!=200 ) {
                    window.alert("Error " + req.status + ": " + req.responseText);
                } else {
                    loadMessages();
                }
            } else
            if (req.readyState > 1 && req.status != 200 && req.status != 304) {
                window.alert("Server responded with an error - please contact Barri.");
            }
        }
    req.open("POST", "http://"+serverUrl+":8002/ticketserver/cancel-broadcast", true);
    req.send();
}

