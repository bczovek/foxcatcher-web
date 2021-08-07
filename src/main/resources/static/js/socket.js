const url = 'http://localhost:8080';
const foxImg = "<img src='https://emojipedia-us.s3.dualstack.us-west-1.amazonaws.com/thumbs/240/twitter/282/fox_1f98a.png' width='50' height='50'>";
const dogImg = "<img src='https://emojipedia-us.s3.dualstack.us-west-1.amazonaws.com/thumbs/240/twitter/282/dog-face_1f436.png' width='50' height='50'>";
const crownImg = "<img src='https://emojipedia-us.s3.dualstack.us-west-1.amazonaws.com/thumbs/240/twitter/282/crown_1f451.png' width='50' height='50'>"
let stompClient;
let gameId;
let playerName;
let playerSide;

function setName(){
    playerName = document.getElementById("nameInput").value;

    if(playerName == null || playerName === ""){
        alert("Please enter a name!");
    }
    else {
        document.getElementById("name").innerHTML = "Your name: " + playerName;
        document.getElementById("createButton").disabled = false;
        document.getElementById("joinButton").disabled = false;
    }
}

function connectToSocket(gameId){
    let socket = new SockJS(url+"/gameplay");
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        stompClient.subscribe("/topic/game-progress/"+gameId, function (response){
            let data = JSON.parse(response.body);
            console.log(data);
            displayResponse(data);
        },{"name": playerName});
    });
}

function createGame(){
    const rbs = document.querySelectorAll('input[name="side"]');
    for(const rb of rbs){
        if(rb.checked){
            playerSide = rb.value;
            break;
        }
    }
    $.ajax({
        url: url+"/game/start",
        type: "POST",
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            "name": playerName,
            "side": playerSide
        }),
        success: function(data){
            gameId = data.gameId;
            playingSide = data.playingSide;
            gameStatus = data.status;
            setBoard(data.dogsPosition, data.foxPosition);
            document.getElementById("error").innerHTML = "";
            document.getElementById("player1Winner").innerHTML = "";
            document.getElementById("player2Winner").innerHTML = "";
            document.getElementById("player2Side").innerHTML = "";
            document.getElementById("gameId").innerHTML = "Game ID: "+gameId;
            document.getElementById("gameStatus").innerHTML="Status: " + data.status;
            document.getElementById("player1").innerHTML = data.player1.name;
            document.getElementById("player1Side").innerHTML = data.player1.side === "FOX" ? foxImg : dogImg;
            document.getElementById("createButton").disabled = true;
            document.getElementById("joinButton").disabled = true;
            document.getElementById("setName").disabled = true;
            document.getElementById("player2").innerHTML = "Waiting for opponent";
            document.getElementById("resetButton").style.display="block";
            connectToSocket(gameId);
        },
        error: function (error){
            console.log(error);
        }
    });
}

function connectToGame() {
    gameId = document.getElementById("gameIdInput").value;
    if(gameId == null || gameId === ''){
        alert("Please enter game ID");
    }else {
        $.ajax({
            url: url + "/game/connect",
            type: 'POST',
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify({
                "name": playerName,
                "gameId": gameId
            }),
            success: function (data) {
                gameId = data.gameId;
                playerSide = data.player2.side;
                playingSide = data.playingSide;
                gameStatus = data.status;
                setBoard(data.dogsPosition, data.foxPosition);
                document.getElementById("error").innerHTML = "";
                document.getElementById("player1Winner").innerHTML = "";
                document.getElementById("player2Winner").innerHTML = "";
                document.getElementById("gameId").innerHTML = "Game ID: "+gameId;
                document.getElementById("gameStatus").innerHTML="Status: " + data.status;
                document.getElementById("resetButton").style.display="block";
                document.getElementById("createButton").disabled = true;
                document.getElementById("joinButton").disabled = true;
                document.getElementById("setName").disabled = true;
                document.getElementById("player1").innerHTML = data.player1.name;
                document.getElementById("player1Side").innerHTML = data.player1.side === "FOX" ? foxImg : dogImg;
                document.getElementById("player2").innerHTML = data.player2.name;
                document.getElementById("player2Side").innerHTML = data.player2.side === "FOX" ? foxImg : dogImg;
                connectToSocket(gameId);
                alert("Connected");
            },
            error: function (error) {
                document.getElementById("error").innerHTML = "Couldn't join game";
            }
        });
    }
}