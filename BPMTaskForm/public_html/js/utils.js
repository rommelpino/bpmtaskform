//GENERIC FUNCTION KEYS 
var keyRegistry = new Array();
keyRegistry[0] = "F9";
//other samples
//keyRegistry[0]="F1"; 
//keyRegistry[1]="ctrl K"; 
//keyRegistry[2]="ctrl L"; 
//keyRegistry[3]="ctrl N"; 
//keyRegistry[4]="ctrl D"; 
//keyRegistry[5]="F4"; 
//keyRegistry[6]="F5"; 
//keyRegistry[7]="F8"; 
//keyRegistry[8]="F10"; 
//keyRegistry[9]="F11";
function registerKeyBoardHandler(serverListener, afdocument) {
    _serverListener = serverListener;
    var document = AdfPage.PAGE.findComponentByAbsoluteId(afdocument);
    _document = document;
    //iterate over an array of keyboard shortcuts to register with 
    //the document
    for (var i = keyRegistry.length - 1;i >= 0;i--) {
        var keyStroke = AdfKeyStroke.getKeyStrokeFromMarshalledString(keyRegistry[i]);
        AdfRichUIPeer.registerKeyStroke(document, keyStroke, callBack);
    }
}

function callBack(adfKeyStroke) {
    var activeComponentClientId = AdfPage.PAGE.getActiveComponentId();
    var activeComponent = AdfPage.PAGE.getActiveComponent();
    //send the marshalled key code to the server listener for the 
    //developer to handle the function key in a managed bean method
    if (adfKeyStroke.getKeyCode() == AdfKeyStroke.F9_KEY && activeComponent != null && activeComponent.getTypeName() == "AdfRichInputListOfValues") {
        if (activeComponent.getProperty('disabled') != null && !activeComponent.getProperty('disabled')) {
            AdfLaunchPopupEvent.queue(activeComponent, true);
        }
        return true;
    }
    

    //if key will be handled on server
    //not yet used
    var marshalledKeyCode = adfKeyStroke.toMarshalledString();
    AdfCustomEvent.queue(_document, _serverListener, 
    {
        keycode : marshalledKeyCode, activeComponentClientId : activeComponentClientId
    },
false);
    //indicate to the client that the key was handled and that 
    //there is no need to pass the event to the browser to handle it
    return true;
 
}