
var nextTickets = {};

function loadTickets() {
    loadTicketsFor("female");
    loadTicketsFor("male");
}

function loadTicketsFor(sex) {
    var req = new XMLHttpRequest();
    req.onreadystatechange = function() {
        if ( req.readyState==4 && req.status==200) {
            var p = document.getElementById(sex+"Ticket");
            while (p.firstChild) {
                p.removeChild(p.firstChild);
            }
            var t = document.createTextNode(req.responseText);
            p.appendChild(t);
            nextTickets[sex] = parseInt(req.responseText);
        }
        if (req.readyState > 1 && req.status != 200 && req.status != 304) {
            window.alert("Error Loading Shower Tickers! Please contact Barri.");
        }
    }
    req.open("GET", "http://"+serverUrl+":8002/ticketserver/shower/"+sex, true);
    req.send();
}

function userInputTicketNumber(sex) {
    var nextNumberString = prompt("Enter the next ticket number for " + sex.toUpperCase());
    var nextNumber = parseInt(nextNumberString);
    if ( !isNaN(nextNumber) ) {
        postTicketNumbers(sex, nextNumber);
    }
}

function nextTicket(sex) {
    postTicketNumbers(sex, nextTickets[sex]+1);
}

function postTicketNumbers(sex, number) {
    var req = new XMLHttpRequest();
    req.onreadystatechange = function() {
        if ( req.readyState==4 ) {
            if ( req.status!=200 ) {
                window.alert("Server responded with error code " + req.status + "! Please contact Barri.");
            }
            loadTickets();
        }
        if (req.readyState > 1 && req.status != 200 && req.status != 304) {
            window.alert("Server responded with an error. Please contact Barri.");
            loadTickets();
        }
    }
    req.open("POST", "http://"+serverUrl+":8002/ticketserver/shower/"+sex+"?number="+number, true);
    req.send();
}

