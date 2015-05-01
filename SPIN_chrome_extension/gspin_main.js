document.addEventListener('DOMContentLoaded', function () {
  chrome.runtime.onMessage.addListener(
    function(msg, sender, sendResponse) {
      console.log(msg);
      msg.isGraph = true;
      msg.isDialog = true;
      sendRequest(msg);
  });
});

function sendRequest(msg) {
  interactiveSignIn(function(token) {
    sendSpinRequest(msg.threadID, token, function(response) {
      //console.log(response);
      var data = JSON.parse(response);
      var edges = JSON.parse(data.graph);
      var emails = JSON.parse(data.emails);
      console.log(edges);
      
      $('#loading').hide();
      
      if (msg.isGraph)
        showGraph(edges);

      if (msg.isDialog)
        showDialog(emails);
      /*
      renderContent(data.html);
      var cb1 = $("#request-cb");
      cb1.addEventListener("click", function() {handleCheckbox(cb1)});
      var cb2 = $("#conventional-cb");
      cb2.addEventListener("click", function() {handleCheckbox(cb2)});
      var cb3 = $("#inform-cb");
      cb3.addEventListener("click", function() {handleCheckbox(cb3)});
      */   
    });
  });
}

function interactiveSignIn(callback) {
  chrome.identity.getAuthToken({ 'interactive': true }, function(token) {
    if (chrome.runtime.lastError)
      console.log(chrome.runtime.lastError);
    else
      callback(token);
  });
}

function sendSpinRequest(threadID, token, callback) {
  var serverURL = 'http://localhost:8230';
  var x = new XMLHttpRequest();  
  x.open('POST', serverURL, true);

  x.onload = function() {
    var response = x.response; 
    callback(response);
  };
  
  //x.onerror = function() {
  //  errorCallback('Network error.');
  //};
  var request = "threadID=" + encodeURIComponent(threadID) + "&token=" + encodeURIComponent(token);
  x.send(request);
}

function showGraph(edges) {
  var l = edges.length;
  var names = [];

  for (var i = 0; i < l; i++) {
    var pair = edges[i];
    names.push(pair[0]);
    names.push(pair[1]);
  }

  var uniqueNames = [];
  $.each(names, function(i, el){
    if ($.inArray(el, uniqueNames) === -1) 
      uniqueNames.push(el);
  });
  console.log(uniqueNames);
  console.log(edges);
  var graphJSON = {
      "nodes": uniqueNames,
      "edges": edges
  };

  jQuery(function(){
    var graph = new Springy.Graph();
    graph.loadJSON(graphJSON);

    var springy = $('#springydemo').springy({
      graph: graph
    });
  });
}

function showDialog(emails) {
  console.log(emails);
  emails = {emails: emails};
  var template = $('#emails').html();
  console.log(template);
  var html = Mustache.to_html(template, emails);
  console.log(html);
  $('#email-panel').html(html);
}

function handleCheckbox(cb) {
  var cls = cb.value;
  if (cb.checked) {
    $(cls).show();
  }
  else {
    $(cls).hide();
  }
}

//clearResults();
    /* 
     if(!$('#powerpred').prop('checked') && !$('#dialoganal').prop('checked')) {
     renderError("Select at least one analysis option!");
     return;
     }
     */
     //    renderContent(document.querySelector('powerpred'))
           //renderContent("<div class=\"loading\"><div class=\"progress\"><div class=\"progress-bar progress-bar-striped active\" role=\"progressbar\" aria-valuenow=\"45\" aria-valuemin=\"0\" aria-valuemax=\"100\"><span class=\"sr-only\">Processing...</span></div></div></div>")
     //var threadID = url.match(/.+#inbox\/(.+)/)[1];
/*   
    interactiveSignIn(function(token) {
      sendSpinRequest(threadID, token, function(response) {
        //console.log(response);
        var data = JSON.parse(response);
        var edges = JSON.parse(data.graph);
        console.log(edges);

        var l = edges.length;

        var names = [];
        for (var i = 0; i < l; i++) {
          var pair = edges[i];
          names.push(pair[0]);
          names.push(pair[1]);
        }

        var uniqueNames = [];
        $.each(names, function(i, el){
          if ($.inArray(el, uniqueNames) === -1) 
            uniqueNames.push(el);
        });
        console.log(uniqueNames);
        console.log(edges);
        var graphJSON = {
            "nodes": uniqueNames,
            "edges": edges
        };

      if($('#dialoganal').prop('checked')) {

      renderContent(data.html);
      var cb1 = document.getElementById("request-cb");
      cb1.addEventListener("click", function() {handleCheckbox(cb1)});
      var cb2 = document.getElementById("conventional-cb");
      cb2.addEventListener("click", function() {handleCheckbox(cb2)});
      var cb3 = document.getElementById("inform-cb");
      cb3.addEventListener("click", function() {handleCheckbox(cb3)});
     }
      else {
      renderContent("");
      }
                      
      if($('#powerpred').prop('checked')) {

        document.getElementById('power-graph').innerHTML = "<canvas id=\"springydemo\" width=\"700px\"/>";

        jQuery(function(){
          var graph = new Springy.Graph();
          graph.loadJSON(graphJSON);

          var springy = jQuery('#springydemo').springy({
            graph: graph
          });
        });
      }
        

      });
    });
    //renderStatus('Performing Google Image search for ' + msgID);



    }, function(errorMessage) {
      renderContent('Cannot display image. ' + errorMessage + '\n' + threadID);
  });   

*/