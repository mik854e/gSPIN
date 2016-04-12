document.addEventListener('DOMContentLoaded', function () {
  document.querySelector('button').addEventListener('click', clickHandler);
  isGmailThread();
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
    var one_part = url.match(/.+#[A-z]+\/([^/]+)$/);
    var two_part = url.match(/.+#[A-z]+\/[^/]+\/([^/]+)$/);
    
    var threadID;
    if (one_part)
      threadID = one_part[1];
    else if (two_part)
      threadID = two_part[1];
    else {
      renderContent("Select a Gmail thread.");
      return;
    }

    var msg = {threadID: threadID,
               isDialog: $('#dialoganal').prop('checked'),
               isGraph: $('#powerpred').prop('checked')};

    chrome.windows.create({
      url: 'html/gspin_main.html#' + JSON.stringify(msg),
      type: 'popup',
      width: 700,
      height: 200
    }); 
  });  
}

function isGmailThread() {
  getCurrentTabUrl(function(url) {
    // URIs seem to have either one or two parts before the 
    // threadID
    //  - #spam/1540840f836336ea
    //  - #search/searchquery/153de17a073a9cb9
    var one_part = url.match(/.+#[A-z]+\/(.+)/);
    var two_part = url.match(/.+#[A-z]+\/.+\/(.+)/);

    if (!one_part && !two_part)
      renderContent();
  });
}              

function renderContent() {
  $('#options').hide();
  $('#analyze').hide();
  $('#popup-msg').show();
}