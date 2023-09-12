<script type='text/ecmascript'><![CDATA[

  var efBoxes = [];
  var eSvg = null;

////////// loadSVG()
  function loadSVG() {
    efBoxes = getElementsByClassName('box', document.getElementsByTagName('g'));
    eSvg = document.getElementById('svg');
    expandAll();
  }

////////// getElementsByClassName(string, nodeList)
  function getElementsByClassName(sClass, nlNodes) {
    var elements = [];
    for (var i=0; i<nlNodes.length; i++) {
      if(nlNodes.item(i).nodeType==1 && sClass==nlNodes.item(i).getAttribute('class')) {
        elements.push(nlNodes.item(i));
      }
    }
    return elements;
  }

////////// show(string)
  function show(sId) {
    var useElement = document.getElementById('s'+sId);
    var moveNext = false;
    var eBoxLast;
    var maxX = 500;
    
    if (notPlus(useElement)) {
      eBoxLast = document.getElementById(sId);
      setPlus(useElement);
      for (var i=0; i<efBoxes.length; i++) {
        var eBox = efBoxes[i];
        if (moveNext) {
          move(eBoxLast, eBox);
        }
        else if (isDescendant(sId, eBox.id)) {
          eBox.setAttribute('visibility', 'hidden');
        }
        else if (isHigherBranch(sId, eBox.id)) {
          move(eBoxLast, eBox);
          moveNext = true;
        }
        if (eBox.getAttribute('visibility') != 'hidden') {
          eBoxLast = eBox;
          x = xTrans(eBox);
          if (x > maxX) maxX = x;
        }
      }
    }

    else {
      setMinus(useElement);
      var skipDescendantsOf;
      for (var i=0; i<efBoxes.length; i++) {
        var eBox = efBoxes[i];
        if (moveNext) {
          move(eBoxLast, eBox);
        }
        else if (isDescendant(sId, eBox.id) && (!skipDescendantsOf || !isDescendant(skipDescendantsOf.id, eBox.id))) {
          eBox.setAttribute('visibility', 'visible');
          move(eBoxLast, eBox);
          if (nextClosed(eBox)) skipDescendantsOf = eBox;
        }
        else if (isHigherBranch(sId, eBox.id)) {
          move(eBoxLast, eBox);
          moveNext = true;
        }
        if (eBox.getAttribute('visibility') != 'hidden') {
          eBoxLast = eBox;
          x = xTrans(eBox);
          if (x > maxX) maxX = x;
        }
      }
    }
    //setHeight(yTrans(eBoxLast)+"+(AbstractSymbol.MAX_HEIGHT+AbstractSymbol.Y_INDENT)+");\n"+
    setHeight(yTrans(eBoxLast)+%HEIGHT_SUM%);
    setWidth(maxX+360);
  }

////////// collapseAll()
  function collapseAll() {
    for (var i=0; i<efBoxes.length; i++) {
      var eBox = efBoxes[i];
      var useElement = document.getElementById('s'+eBox.id);
      if (useElement) setPlus(useElement);
      if (eBox.id != '_1') eBox.setAttribute('visibility', 'hidden');
    }
    setHeight(400);
    setWidth(500);
  }

////////// expandAll()
  function expandAll() {
    var eBoxLast;
    var maxX = 0;
    for (var i=0; i<efBoxes.length; i++) {
      var eBox = efBoxes[i];
      var useElement = document.getElementById('s'+eBox.id);
      if (useElement) setMinus(useElement);
      move(eBoxLast, eBox);
      eBox.setAttribute('visibility', 'visible');
      eBoxLast = eBox;
      var x = xTrans(eBox);
      if (x > maxX) maxX = x;
    }
    //setHeight(yTrans(eBoxLast)+"+(AbstractSymbol.MAX_HEIGHT+AbstractSymbol.Y_INDENT)+");\n"+
    setHeight(yTrans(eBoxLast)+%HEIGHT_SUM%);
    setWidth(maxX+360);
  }

////////// makeVisible(string)
  function makeVisible(sId) {
    var childNodes = document.getElementById(sId).childNodes;
    var hidden = getElementsByClassName('hidden', childNodes);
    var visible = getElementsByClassName('visible', childNodes);
    inheritVisibility(hidden);
    hiddenVisibility(visible);
  }

////////// makeHidden(string)
  function makeHidden(sId) {
    var childNodes = document.getElementById(sId).childNodes;
    var hidden = getElementsByClassName('hidden', childNodes);
    var visible = getElementsByClassName('visible', childNodes);
    inheritVisibility(visible);
    hiddenVisibility(hidden);
  }

////////// inheritVisibility(element[])
  function inheritVisibility(efElements) {
    for (var i=0; i<efElements.length; i++) {
      efElements[i].setAttribute('visibility', 'inherit');
    }
  }

////////// hiddenVisibility(element[])
  function hiddenVisibility(efElements) {
    for (var i=0; i<efElements.length; i++) {
      efElements[i].setAttribute('visibility', 'hidden');
    }
  }

////////// nextClosed(element)
  function nextClosed(eBox) {
    var useElement = document.getElementById('s'+eBox.id);
    return (useElement && !notPlus(useElement));
  }

////////// isHigherBranch(string, string)
  function isHigherBranch(sSerialLower, sSerialHigher) {
    var sLower = sSerialLower.split('_');
    var sHigher = sSerialHigher.split('_');
    for (var i=0; i<sLower.length; i++) {
      if (Number(sHigher[i]) > Number(sLower[i])) return true;
      else if (Number(sHigher[i]) < Number(sLower[i])) return false;
    }
    return false;
  }

////////// isOnHigherLevel(element, element)
  function isOnHigherLevel(eBoxLower, eBoxHigher) {
    var sLower = eBoxLower.id.split('_');
    var sHigher = eBoxHigher.id.split('_');
    for (var i=0; i<sLower.length; i++) {
      if (Number(sHigher[i]) > Number(sLower[i])) return true;
    }
    return false;
  }

////////// isDescendant(string, string)
  function isDescendant(sSerialAsc, sSerialDesc) {
    return (sSerialDesc.length > sSerialAsc.length && sSerialDesc.indexOf(sSerialAsc) === 0);
  }

////////// getParent(element)
  function getParent(eBox) {
    var serial = eBox.id.substring(0, eBox.id.lastIndexOf('_'));
    return document.getElementById(serial);
  }

////////// move(element, element)
  function move(eBoxLast, eBox) {
    if (!eBoxLast) return;
    if (isOnHigherLevel(eBoxLast, eBox)) {
      //setYTrans(eBox, yTrans(eBoxLast)+"+(AbstractSymbol.MAX_HEIGHT+AbstractSymbol.Y_INDENT)+");\n"+
      var attDescHeight = eBoxLast.getAttribute('data-desc-height-rest');
      var attDescX = Number(eBoxLast.getAttribute('data-desc-x'));
      var attX = xTrans(eBox);
      var descHeight = Number(attDescHeight);
      var heightAddon = 0;

      var currWidth = eBox.getElementsByClassName("shadow")[0];
      if (currWidth) {
        currWidth = Number(currWidth.getAttribute("width"));
      } else {
        currWidth = 0;
      }
      if(descHeight && ((attDescX >= attX && attDescX < attX + currWidth) || (attX < attDescX))) heightAddon = descHeight;

      setYTrans(eBox, yTrans(eBoxLast)+%HEIGHT_SUM%+heightAddon);
      var parent = getParent(eBox);
      var line = document.getElementById('p'+eBox.id);
      if (!parent || !line) return;
		//"      line.y1.baseVal.valueInSpecifiedUnits = yTrans(parent)-yTrans(eBox)+"+AbstractSymbol.MAX_HEIGHT/2+";
      //line.setAttribute('y1', String(yTrans(parent)-yTrans(eBox)+"+AbstractSymbol.MAX_HEIGHT/2+"));\n"+
      line.setAttribute('y1', String(yTrans(parent)-yTrans(eBox)+%HEIGHT_HALF%));
    }
    else {
      setYTrans(eBox, yTrans(eBoxLast));
    }
  }

////////// notPlus(element)
  function notPlus(eUseElement) {
    return (eUseElement.getAttributeNS('http://www.w3.org/1999/xlink', 'href') != '#plus');
  }

////////// setPlus(element)
  function setPlus(eUseElement) {
    eUseElement.setAttributeNS('http://www.w3.org/1999/xlink', 'href', '#plus');
  }

////////// setMinus(element)
  function setMinus(eUseElement) {
    eUseElement.setAttributeNS('http://www.w3.org/1999/xlink', 'href', '#minus');
  }

////////// setHeight(number)
  function setHeight(nHeight) {
    eSvg.setAttribute('height', nHeight);
  }

////////// setWidth(number)
  function setWidth(nWidth) {
    eSvg.setAttribute('width', nWidth);
  }

////////// xyTrans(element)
  function xTrans(eBox) {
		//"    return eBox.transform.baseVal.getItem(0).matrix.e;
    var transform = eBox.getAttribute('transform');
    var x = Number(transform.substring(10, Number(transform.length)-1).split(',')[0]);
    if(!x) x = 0;
    return x;
  }

////////// yTrans(element)
  function yTrans(eBox) {
		//"    return eBox.transform.baseVal.getItem(0).matrix.f;
    var transform = eBox.getAttribute('transform');
    var y = Number(transform.substring(10, Number(transform.length)-1).split(',')[1]);
    if(!y) y = 0;
    return y;
  }

////////// setYTrans(element, number)
  function setYTrans(eBox, nValue) {
		//"    return eBox.transform.baseVal.getItem(0).matrix.f = nValue;
    eBox.setAttribute('transform', 'translate('+xTrans(eBox)+','+nValue+')');
  }

]]></script>