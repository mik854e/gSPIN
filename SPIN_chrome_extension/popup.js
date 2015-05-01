document.addEventListener('DOMContentLoaded', function () {
  document.querySelector('button').addEventListener('click', clickHandler);
});

function getCurrentTabUrl(callback) {
  var queryInfo = {
    active: true,
    currentWindow: true
  };

  chrome.tabs.query(queryInfo, function(tabs) {
    var tab = tabs[0];
    var url = tab.url;
    console.assert(typeof url == 'string', 'tab.url should be a string');
    callback(url);
  });
}

function clickHandler() {
  getCurrentTabUrl(function(url) {
    chrome.windows.create({
        url: 'html/gspin_main.html',
        type: 'popup',
        //title: 'gSPIN',
        width: 700,
        height: 700
    }); 
        
    var inbox = url.match(/.+#inbox\/(.+)/);
    var search = url.match(/.+#search\/.+\/(.+)/);
    var threadID;

    if (inbox)
      threadID = inbox[1];
    else if (search)
      threadID = search[1];
    else {
      renderContent("Select a Gmail email thread.");
      return;
    }

    var msg = {threadID: threadID};
    chrome.runtime.sendMessage(msg);
  });  
}              

//document.addEventListener('DOMContentLoaded', function() {
//  getCurrentTabUrl(function(url) {
//
//    //var threadID = url.match(/.+#inbox\/(.+)/)[1];
//    var inbox = url.match(/.+#inbox\/(.+)/);
//    var search = url.match(/.+#search\/.+\/(.+)/);
//    var threadID;
//
//    if (inbox) {
//      threadID = inbox[1];
//    }
//    else if (search) {
//      threadID = search[1];
//    }
//    else {
//      renderContent("Select a Gmail email thread.")
//      return;
//    }
//
//    interactiveSignIn(function(token) {
//      sendSpinRequest(threadID, token, function(response) {
//        //renderStatus('Message ID: ' + msgID + '\n' +
//        //    'Google image search result: ' + msgID);
//        //var imageResult = document.getElementById('image-result');
//        //console.log(response);
//        var data = JSON.parse(response);
//        var edges = JSON.parse(data.graph);
//        console.log(edges);
//
//        var l = edges.length;
//
//        var names = [];
//        for (var i = 0; i < l; i++) {
//          var pair = edges[i];
//          names.push(pair[0]);
//          names.push(pair[1]);
//        }
//
//        var uniqueNames = [];
//        $.each(names, function(i, el){
//          if ($.inArray(el, uniqueNames) === -1) 
//            uniqueNames.push(el);
//        });
//        console.log(uniqueNames);
//        console.log(edges);
//        var graphJSON = {
//            "nodes": uniqueNames,
//            "edges": edges
//        };
//
//      renderContent(data.html);
//      var cb1 = document.getElementById("request-cb");
//      cb1.addEventListener("click", function() {handleCheckbox(cb1)});
//      var cb2 = document.getElementById("conventional-cb");
//      cb2.addEventListener("click", function() {handleCheckbox(cb2)});
//      var cb3 = document.getElementById("inform-cb");
//      cb3.addEventListener("click", function() {handleCheckbox(cb3)});
//
//        jQuery(function(){
//          var graph = new Springy.Graph();
//          graph.loadJSON(graphJSON);
//
//          var springy = jQuery('#springydemo').springy({
//            graph: graph
//          });
//        });
//
//        
//
//      });
//    });
//    //renderStatus('Performing Google Image search for ' + msgID);
//
//
//
//    }, function(errorMessage) {
//      renderContent('Cannot display image. ' + errorMessage + '\n' + threadID);
//  });
//});
