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
    var inbox = url.match(/.+#inbox\/(.+)/);
    var search = url.match(/.+#search\/.+\/(.+)/);
    var threadID;

    if (inbox)
      threadID = inbox[1];
    else if (search)
      threadID = search[1];
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
      var inbox = url.match(/.+#inbox\/(.+)/);
      var search = url.match(/.+#search\/.+\/(.+)/);

      if (!inbox && !search)
        renderContent();
  });
}              

function renderContent() {
  $('#options').hide();
  $('#analyze').hide();
  $('#popup-msg').show();
}