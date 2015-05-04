document.addEventListener('DOMContentLoaded', function () {
  chrome.runtime.onMessage.addListener(
    function(msg, sender, sendResponse) {
      console.log(msg);
      sendRequest(msg);
  });

  $("#request-cb").on("click", function() {handleCheckbox(this)});
  $("#conventional-cb").on("click", function() {handleCheckbox(this)});
  $("#inform-cb").on("click", function() {handleCheckbox(this)});
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

  $('powergraph').show();
}

function showDialog(emails) {
  console.log(emails);
  emails = {emails: emails};
  var template = $('#emails').html();
  console.log(template);
  var html = Mustache.to_html(template, emails);
  console.log(html);
  $('.emails').html(html);
  $('#email-panel').show();
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