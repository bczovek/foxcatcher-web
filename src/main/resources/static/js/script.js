let gfoxPosition;
let gdogsPosition;
let playingSide;
let gameStatus;
let moving = false;
let possibleMoves;
let selected;


function setBoard(dogsPosition, foxPosition){
    gfoxPosition = foxPosition;
    gdogsPosition = dogsPosition;
    $("td").text("");
    document.getElementById(foxPosition.row+'-'+foxPosition.col).innerHTML= foxImg;
    dogsPosition.forEach(position => {
        document.getElementById(position.row+'-'+position.col).innerHTML = dogImg;
    });
}

function displayResponse(data){
    document.getElementById("gameStatus").innerHTML="Status: " + data.status;
    document.getElementById("player2").innerHTML = data.player2 != null ? data.player2.name : "";
    document.getElementById("player2Side").innerHTML = data.player2 != null ? data.player2.side === "FOX" ? foxImg : dogImg : "";
    setBoard(data.dogsPosition, data.foxPosition);
    playingSide = data.playingSide;
    gameStatus = data.status;
    if(data.winner != null){
        showWinner(data);
    }
}

$("td").click(function (){
    let tile = $(this).attr('id').split('-');
    let selectedPosition = {
        "row": parseInt(tile[0]),
        "col": parseInt(tile[1])
    }
    if(playerSide === playingSide && !moving && gameStatus === "IN_PROGRESS") {
        switch (playerSide){
            case "FOX":
                selectFox(selectedPosition);
                break;
            case "DOGS":
                selectDog(selectedPosition);
                break;
        }
    } else if(moving){
        console.log(selected);
        console.log(selectedPosition);
        if(selected.row === selectedPosition.row && selected.col === selectedPosition.col){
            $("#"+selected.row+"-"+selected.col).css("background", "");
            for(position of possibleMoves){
                $("#" + position.row + "-" + position.col).css("background", "");
            }
            moving = false;

        }else {
            for (position of possibleMoves) {
                if (position.row === selectedPosition.row && position.col === selectedPosition.col) {
                    $.ajax({
                        url: url + "/game/gameplay",
                        type: "POST",
                        dataType: "json",
                        contentType: "application/json",
                        data: JSON.stringify({
                            "side": playerSide,
                            "from": selected,
                            "to": selectedPosition,
                            "gameId": gameId
                        }),
                        success: function (data) {
                            moving = false;
                            $("#" + selected.row + "-" + selected.col).css("background", "");
                            for (position of possibleMoves) {
                                $("#" + position.row + "-" + position.col).css("background", "");
                            }
                            displayResponse(data);
                        },
                        error: function (error) {
                            console.log(error);
                        }
                    });
                }
            }
        }
    }

});

function selectFox(selectedPosition){

    if(gfoxPosition.row === selectedPosition.row && gfoxPosition.col === selectedPosition.col){
        $.ajax({
            url: url+"/game/select",
            type: "POST",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify({
                "dogsPosition": gdogsPosition,
                "foxPosition": gfoxPosition,
                "selected": selectedPosition,
                "side": playerSide
            }),
            success: function(data){
                showPossibleMoves(data, selectedPosition);
                moving = true;
            },
            error: function (error){
                console.log(error);
            }
        });
    }

}

function selectDog(selectedPosition){

    for(position of gdogsPosition){
        if(position.row === selectedPosition.row && position.col === selectedPosition.col){
            $.ajax({
                url: url+"/game/select",
                type: "POST",
                dataType: "json",
                contentType: "application/json",
                data: JSON.stringify({
                    "dogsPosition": gdogsPosition,
                    "foxPosition": gfoxPosition,
                    "selected": selectedPosition,
                    "side": playerSide
                }),
                success: function(data){
                    showPossibleMoves(data, selectedPosition);
                    moving = true;
                },
                error: function (error){
                    console.log(error);
                }
            });
            break;
        }
    }

}

function showPossibleMoves(data ,selectedPosition){
    possibleMoves = data;
    selected = selectedPosition;
    $("#"+selectedPosition.row+"-"+selectedPosition.col).css("background", "darkred");
    for(position of data){
        $("#"+position.row+"-"+position.col).css("background", "red");
    }
}

function showWinner(data){

    if(data.winner.name === data.player1.name){
        document.getElementById("player1Winner").innerHTML = crownImg;
    }else{
        document.getElementById("player2Winner").innerHTML = crownImg;
    }

}

function reset(){
    if(gameStatus === "FINISHED") {
        stompClient.disconnect(function () {
            console.log("Disconnected");
        }, {});
        document.getElementById("setName").disabled = false;
        document.getElementById("joinButton").disabled = false;
        document.getElementById("createButton").disabled = false;
        document.getElementById("resetButton").style.display = "none";
    } else {
        $.ajax({
            url: url + "/game/disconnect",
            type: "POST",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify({
                "player": {
                    "name": playerName,
                    "side": playerSide
                },
                "gameId": gameId
            }),
            success: function (data) {
                stompClient.disconnect(function () {
                    console.log("Disconnected");
                }, {});
                displayResponse(data);
                document.getElementById("setName").disabled = false;
                document.getElementById("joinButton").disabled = false;
                document.getElementById("createButton").disabled = false;
                document.getElementById("resetButton").style.display = "none";
            },
            error: function (error) {
                console.log(error);
            }
        });
    }
}
