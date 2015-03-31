/**
 * Get the current URL.
 *
 * @param {function(string)} callback - called when the URL of the current tab
 *   is found.
 **/
function getCurrentTabUrl(callback) {
  // Query filter to be passed to chrome.tabs.query - see
  // https://developer.chrome.com/extensions/tabs#method-query
  var queryInfo = {
    active: true,
    currentWindow: true
  };

  chrome.tabs.query(queryInfo, function(tabs) {
    // chrome.tabs.query invokes the callback with a list of tabs that match the
    // query. When the popup is opened, there is certainly a window and at least
    // one tab, so we can safely assume that |tabs| is a non-empty array.
    // A window can only have one active tab at a time, so the array consists of
    // exactly one tab.
    var tab = tabs[0];

    // A tab is a plain object that provides information about the tab.
    // See https://developer.chrome.com/extensions/tabs#type-Tab
    var url = tab.url;

    // tab.url is only available if the "activeTab" permission is declared.
    // If you want to see the URL of other tabs (e.g. after removing active:true
    // from |queryInfo|), then the "tabs" permission is required to see their
    // "url" properties.
    console.assert(typeof url == 'string', 'tab.url should be a string');

    callback(url);
  });
}

/**
 * @param {string} searchTerm - Search term for Google Image search.
 * @param {function(string,number,number)} callback - Called when an image has
 *   been found. The callback gets the URL, width and height of the image.
 * @param {function(string)} errorCallback - Called when the image is not found.
 *   The callback gets a string that describes the failure reason.
 */
function getImageUrl(searchTerm, callback, errorCallback) {
  // Google image search - 100 searches per day.
  // https://developers.google.com/image-search/
  var searchUrl = 'https://ajax.googleapis.com/ajax/services/search/images' +
    '?v=1.0&q=' + encodeURIComponent('dachshund puppy');
  var x = new XMLHttpRequest();  x.open('GET', searchUrl);
  // The Google image search API responds with JSON, so let Chrome parse it.
  x.responseType = 'json';
  x.onload = function() {
    // Parse and process the response from Google Image Search.
    var response = x.response;
    if (!response || !response.responseData || !response.responseData.results ||
        response.responseData.results.length === 0) {
      errorCallback('No response from Google Image search!');
      return;
    }
    var randNumMin = 1;
    var randNumMax = response.responseData.results.length;
    var randInt = (Math.floor(Math.random() * (randNumMax - randNumMin + 1)) + randNumMin);

    var firstResult = response.responseData.results[randInt];
    // Take the thumbnail instead of the full image to get an approximately
    // consistent image size.
    var imageUrl = firstResult.tbUrl;
    var width = parseInt(firstResult.tbWidth);
    var height = parseInt(firstResult.tbHeight);
    console.assert(
        typeof imageUrl == 'string' && !isNaN(width) && !isNaN(height),
        'Unexpected respose from the Google Image Search API!');
    callback(imageUrl, width, height);
  };
  x.onerror = function() {
    errorCallback('Network error.');
  };
  x.send();
}

function renderContent(content) {
  document.getElementById('content').innerHTML = content;
}

function renderError(content) {
    document.getElementById('content').style.margin = "50px 10px 20px 30px";
    document.getElementById('content').align = "center";
    document.getElementById('content').style.color = "red";
    //    content = "<div style=\"color:red margin:\"50px 10px 20px 30px\"\" align=\"center\">" + content + "</div>";
    document.getElementById('content').innerHTML = content;
}

function clearResults() {
    document.getElementById('power-graph').innerHTML = "";
    document.getElementById('content').innerHTML = "";
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

function sendSpinRequest(threadID, token, callback) {
  var serverURL = 'http://localhost:8230'; // '';http://localhost:8230'
  var x = new XMLHttpRequest();  
  x.open('POST', serverURL, true);

  x.onload = function() {
    // Parse and process the response from Google Image Search.
    var response = x.response;
    //if (!response || !response.responseData || !response.responseData.results ||
    //    response.responseData.results.length === 0) {
    //  errorCallback('No response from Google Image search!');
    //  return;
    //}
    //renderStatus(response);   
    callback(response);
  };
  
  //x.onerror = function() {
  //  errorCallback('Network error.');
  //};
  var request = "threadID=" + encodeURIComponent(threadID) + "&token=" + encodeURIComponent(token);
  x.send(request);
}

  /**
    Retrieves a valid token. Since this is initiated by the user
    clicking in the Sign In button, we want it to be interactive -
    ie, when no token is found, the auth window is presented to the user.

    Observe that the token does not need to be cached by the app.
    Chrome caches tokens and takes care of renewing when it is expired.
    In that sense, getAuthToken only goes to the server if there is
    no cached token or if it is expired. If you want to force a new
    token (for example when user changes the password on the service)
    you need to call removeCachedAuthToken()
  **/
  function interactiveSignIn(callback) {
    //changeState(STATE_ACQUIRING_AUTHTOKEN);

    // @corecode_begin getAuthToken
    // @description This is the normal flow for authentication/authorization
    // on Google properties. You need to add the oauth2 client_id and scopes
    // to the app manifest. The interactive param indicates if a new window
    // will be opened when the user is not yet authenticated or not.
    // @see http://developer.chrome.com/apps/app_identity.html
    // @see http://developer.chrome.com/apps/identity.html#method-getAuthToken
    chrome.identity.getAuthToken({ 'interactive': true }, function(token) {
      if (chrome.runtime.lastError) {
        //sampleSupport.log(chrome.runtime.lastError);
        console.log(chrome.runtime.lastError);
        //changeState(STATE_START);
      } else {
        //sampleSupport.log('Token acquired:'+token+
        //  '. See chrome://identity-internals for details.');
        //changeState(STATE_AUTHTOKEN_ACQUIRED);
        //document.getElementById('content').textContent = token;
        callback(token);
      }
    });
    // @corecode_end getAuthToken
  }

document.addEventListener('DOMContentLoaded', function () {
                          document.querySelector('button').addEventListener('click', clickHandler);
                          });

function clickHandler() {
    getCurrentTabUrl(function(url) {
                     
     clearResults();
     
     if(!$('#powerpred').prop('checked') && !$('#dialoganal').prop('checked')) {
     renderError("Select at least one analysis option!");
     return;
     }
     
     //    renderContent(document.querySelector('powerpred'))
     //    renderContent("<div class=\"loading\"><div class=\"progress\"><div class=\"progress-bar progress-bar-striped active\" role=\"progressbar\" aria-valuenow=\"45\" aria-valuemin=\"0\" aria-valuemax=\"100\"><span class=\"sr-only\">Processing...</span></div></div></div>")
     renderContent("<div class=\"loading\"><div class=\"progress\"><div class=\"progress-bar progress-bar-striped active\" role=\"progressbar\" aria-valuenow=\"45\" aria-valuemin=\"0\" aria-valuemax=\"100\"><span class=\"sr-only\">Processing...</span></div></div></div>")
     //var threadID = url.match(/.+#inbox\/(.+)/)[1];
     var inbox = url.match(/.+#inbox\/(.+)/);
     var search = url.match(/.+#search\/.+\/(.+)/);
     var threadID;
     
     if (inbox) {
     threadID = inbox[1];
     }
     else if (search) {
     threadID = search[1];
     }
     else {
     renderError("Select a Gmail email thread.")
     return;
     }


    interactiveSignIn(function(token) {
      sendSpinRequest(threadID, token, function(response) {
        //renderStatus('Message ID: ' + msgID + '\n' +
        //    'Google image search result: ' + msgID);
        //var imageResult = document.getElementById('image-result');
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
