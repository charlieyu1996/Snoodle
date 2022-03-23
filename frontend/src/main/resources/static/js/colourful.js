function randomIntFromInterval(min, max) { // min and max included 
  return Math.floor(Math.random() * (max - min + 1) + min)
}

const rndInt = randomIntFromInterval(1, 4)

var currColor = 'blue';
if (rndInt == 1){
  currColor = 'blue';
}else if (rndInt == 2){
  currColor = 'red';
}else if (rndInt == 3){
  currColor = 'yellow';
}else{
  currColor = 'green';
}
$(".card").each(function (i, obj) {
  $(this).addClass(currColor);
  if (currColor == 'blue'){
    currColor = 'red';
  }else if (currColor == 'red'){
    currColor = 'yellow';
  }else if (currColor == 'yellow'){
    currColor = 'green';
  }else if (currColor == 'green'){
    currColor = 'blue';
  }
});

$(".card-body").each(function (i, obj) {
  $(this).on("click", function () {
    // For the boolean value
    var eventSummary = $(this).find('h5').eq(0).text();
    var emailList = $(this).find('p').eq(0).text();
    var startDate = $(this).find('p').eq(1).text();
    var endDate = $(this).find('p').eq(2).text();
    var startTime = $(this).find('p').eq(3).text();
    var endTime = $(this).find('p').eq(4).text();
    var location = $(this).find('p').eq(5).text();
    var eventDetails = $(this).find('p').eq(6).text();

    $("#eventSummary").val(eventSummary);
    $("#emailList").val(emailList);
    $("#startDate").val(startDate);
    $("#endDate").val(endDate);
    $("#startTime").val(startTime);
    $("#endTime").val(endTime);
    $("#location").val(location);
    $("#eventDetails").val(eventDetails);
    console.log(startDate);
  });
});

