/*  Graph JavaScript framework, version 0.0.1
 *  (c) 2006 Aslak Hellesoy <aslak.hellesoy@gmail.com>
 *  (c) 2006 Dave Hoover <dave.hoover@gmail.com>
 *
 *  Ported from Graph::Layouter::Spring in
 *    http://search.cpan.org/~pasky/Graph-Layderer-0.02/
 *  The algorithm is based on a spring-style layouter of a Java-based social
 *  network tracker PieSpy written by Paul Mutton E<lt>paul@jibble.orgE<gt>.
 *
 *  Graph is freely distributable under the terms of an MIT-style license.
 *  For details, see the Graph web site: http://dev.buildpatternd.com/trac
 *
/*--------------------------------------------------------------------------*/

var RADIUS = 39;
var OFFSET_X = -40;
var OFFSET_Y = -42;

var IMG_NORMAL = 80;

function stateZoom(el){
	var newImg = new Image();
	newImg.src = el.src;
	el.parentNode.parentNode.style.zIndex = 100;
	el.style.width = (newImg.width * .4) + "px";
	el.style.height = (newImg.height * .4) + "px";
}


function stateUnzoom(el){
	el.parentNode.parentNode.style.zIndex = 20;
	el.style.width = IMG_NORMAL + "px";
	el.style.height = IMG_NORMAL + "px";
}

function loadState(name){
	document.getElementById('stateFrame').src = "states/" + name + ".html";
	document.getElementById("graphPanel").style.display = "none";
	document.getElementById("stateFrame").style.display = "block";
}

function updateState(newState){
	document.getElementById("currentState").innerHTML= newState;
	document.getElementById("link" + selectedState).style.color = "black";
	document.getElementById("link" + selectedState).style.fontWeight = "normal";
	document.getElementById("img" + selectedState).style.borderColor = "black";

	document.getElementById("link" + newState).style.color = "red";
	document.getElementById("link" + newState).style.fontWeight = "bold";
	document.getElementById("img" + newState).style.borderColor = "red";
	selectedState = newState;
}

function zoom(value){
	currentZoom = currentZoom + value;
	document.getElementById('zoom').innerHTML = currentZoom + "%";
	makeGraph();
}

function toggleGraph(){
	var elGraph = document.getElementById("graphPanel");
	var elStateFrame = document.getElementById("stateFrame");
	if(elGraph.style.display=="none"){
		elGraph.style.display = "block";
		elStateFrame.style.display = "none";
	}else{
		elGraph.style.display = "none";
		elStateFrame.style.display = "block";
	}
}	

var layoutValues = new Array();

function initLayoutValues(){
	for(i=0; i<4; i++){
		layoutValues[i] = 0.1 * Math.random() + 0.1;
	}
}

function getLayoutValue(value){
	if(layoutValues.length == 0){
		initLayoutValues();
	}
	return layoutValues[value - 1];
}

function getWidth() {
  if( typeof( window.innerWidth ) == 'number' ) {
    return window.innerWidth;
  } else {
    return document.documentElement.clientWidth;
  }
}

function getHeight() {
  if( typeof( window.innerWidth ) == 'number' ) {
    return window.innerHeight;
  } else {
	return document.documentElement.clientHeight;
  }
}

var Graph = Class.create();
Graph.prototype = {
        initialize: function() {
                this.nodeSet = {};
                this.nodes = [];
                this.edges = [];
        },
       
        addNode: function(value) {
                if(typeof value == 'string') {
                        // Create a new div
                        var key = value;
                        var element = document.createElement('div');
                        element.innerHTML = value;
                        // THIS DOESN'T WORK!! NEED AN OTHER CONTAINER.
                        document.appendChild(element);
                } else {
                        // Assuming it's a DOM node *with* and id.
                        var key = value.id;
                        var element = value;
                }

                var node = this.nodeSet[key];
                if(node == undefined) {
                        node = new Graph.Node(element);
                        this.nodeSet[key] = node;
                        this.nodes.push(node);
                }
                return node;
        },

        // Uniqueness must be ensured by caller
        addEdge: function(source, target) {
                var s = this.addNode(source);
                var t = this.addNode(target);
                var edge = {source: s, target: t};
                this.edges.push(edge);
        }
};

Graph.Node = Class.create();
Graph.Node.prototype = {
        initialize: function(value) {
                this.value = value;
        }
};

Graph.Renderer = {};
Graph.Renderer.Basic = Class.create();
Graph.Renderer.Basic.prototype = {
        initialize: function(element, graph) {
                this.element = element;
                this.graph = graph;

                this.ctx = element.getContext("2d");
                this.radius = RADIUS;
                this.arrowAngle = Math.PI/10;

                this.factorX = (element.width - 2 * this.radius) / (graph.layoutMaxX - graph.layoutMinX);
                this.factorY = (element.height - 2 * this.radius) / (graph.layoutMaxY - graph.layoutMinY);
        },

        translate: function(point) {
                return [
                  (point[0] - this.graph.layoutMinX) * this.factorX + this.radius,
                  (point[1] - this.graph.layoutMinY) * this.factorY + this.radius
                ];
        },

        rotate: function(point, length, angle) {
                var dx = length * Math.cos(angle);
                var dy = length * Math.sin(angle);
                return [point[0]+dx, point[1]+dy];
        },

        draw: function() {
            for (var i = 0; i < this.graph.nodes.length; i++) {
                        this.drawNode(this.graph.nodes[i]);
                }
            for (var i = 0; i < this.graph.edges.length; i++) {
                        this.drawEdge(this.graph.edges[i]);
                }
        },
       
        drawNode: function(node) {
                var point = this.translate([node.layoutPosX, node.layoutPosY]);

            node.value.style.position = 'absolute';
            node.value.style.top      = point[1] + OFFSET_Y + 'px';
            node.value.style.left     = point[0] + OFFSET_X +'px';
               
                this.ctx.strokeStyle = 'black'
                this.ctx.beginPath();
                this.ctx.arc(point[0], point[1], this.radius, 0, Math.PI*2, true);
                this.ctx.closePath();
                this.ctx.stroke();
        },
       
        drawEdge: function(edge) {
                var source = this.translate([edge.source.layoutPosX, edge.source.layoutPosY]);
                var target = this.translate([edge.target.layoutPosX, edge.target.layoutPosY]);

                var tan = (target[1] - source[1]) / (target[0] - source[0]);
                var theta = Math.atan(tan);
                if(source[0] <= target[0]) {theta = Math.PI+theta}
                source = this.rotate(source, -this.radius, theta);
                target = this.rotate(target, this.radius, theta);

                // draw the edge
                this.ctx.strokeStyle = 'grey';
                this.ctx.fillStyle = 'grey';
                this.ctx.lineWidth = 1.0;
                this.ctx.beginPath();
                this.ctx.moveTo(source[0], source[1]);
                this.ctx.lineTo(target[0], target[1]);
                this.ctx.stroke();
               
                this.drawArrowHead(20, this.arrowAngle, theta, source[0], source[1], target[0], target[1]);
        },

        drawArrowHead: function(length, alpha, theta, startx, starty, endx, endy) {
                var top = this.rotate([endx, endy], length, theta + alpha);
                var bottom = this.rotate([endx, endy], length, theta - alpha);
                this.ctx.beginPath();
                this.ctx.moveTo(endx, endy);
                this.ctx.lineTo(top[0], top[1]);
                this.ctx.lineTo(bottom[0], bottom[1]);
                this.ctx.fill();
        }
};

Graph.Layout = {};
Graph.Layout.Spring = Class.create();
Graph.Layout.Spring.prototype = {
        initialize: function(graph) {
                this.graph = graph;
                this.iterations = 500;
                this.maxRepulsiveForceDistance = 6;
                this.k = 2;
                this.c = 0.01;
                this.maxVertexMovement = 0.5;
        },
       
        layout: function() {
                this.layoutPrepare();
            for (var i = 0; i < this.iterations; i++) {
                        this.layoutIteration();
                }
                this.layoutCalcBounds();
        },
       
        layoutPrepare: function() {
            for (var i = 0; i < this.graph.nodes.length; i++) {
                    var node = this.graph.nodes[i];
                        node.layoutPosX = 0;
                        node.layoutPosY = 0;
                        node.layoutForceX = 0;
                        node.layoutForceY = 0;
                }               
        },
       
        layoutCalcBounds: function() {
                var minx = Infinity, maxx = -Infinity, miny = Infinity, maxy = -Infinity;

            for (var i = 0; i < this.graph.nodes.length; i++) {
                        var x = this.graph.nodes[i].layoutPosX;
                        var y = this.graph.nodes[i].layoutPosY;
                                               
                        if(x > maxx) maxx = x;
                        if(x < minx) minx = x;
                        if(y > maxy) maxy = y;
                        if(y < miny) miny = y;
                }

                this.graph.layoutMinX = minx;
                this.graph.layoutMaxX = maxx;
                this.graph.layoutMinY = miny;
                this.graph.layoutMaxY = maxy;
        },
       
        layoutIteration: function() {
                // Forces on nodes due to node-node repulsions
            for (var i = 0; i < this.graph.nodes.length; i++) {
                    var node1 = this.graph.nodes[i];
                    for (var j = i + 1; j < this.graph.nodes.length; j++) {
                            var node2 = this.graph.nodes[j];
                                this.layoutRepulsive(node1, node2);
                        }
                }
                // Forces on nodes due to edge attractions
            for (var i = 0; i < this.graph.edges.length; i++) {
                    var edge = this.graph.edges[i];
                        this.layoutAttractive(edge);             
                }
               
                // Move by the given force
            for (var i = 0; i < this.graph.nodes.length; i++) {
                    var node = this.graph.nodes[i];
                        var xmove = this.c * node.layoutForceX;
                        var ymove = this.c * node.layoutForceY;

                        var max = this.maxVertexMovement;
                        if(xmove > max) xmove = max;
                        if(xmove < -max) xmove = -max;
                        if(ymove > max) ymove = max;
                        if(ymove < -max) ymove = -max;
                       
                        node.layoutPosX += xmove;
                        node.layoutPosY += ymove;
                        node.layoutForceX = 0;
                        node.layoutForceY = 0;
                }
        },

        layoutRepulsive: function(node1, node2) {
                var dx = node2.layoutPosX - node1.layoutPosX;
                var dy = node2.layoutPosY - node1.layoutPosY;
                var d2 = dx * dx + dy * dy;
                if(d2 < 0.01) {
                        dx = getLayoutValue(1);
                        dy = getLayoutValue(2);
                        var d2 = dx * dx + dy * dy;
                }
                var d = Math.sqrt(d2);
                if(d < this.maxRepulsiveForceDistance) {
                        var repulsiveForce = this.k * this.k / d;
                        node2.layoutForceX += repulsiveForce * dx / d;
                        node2.layoutForceY += repulsiveForce * dy / d;
                        node1.layoutForceX -= repulsiveForce * dx / d;
                        node1.layoutForceY -= repulsiveForce * dy / d;
                }
        },

        layoutAttractive: function(edge) {
                var node1 = edge.source;
                var node2 = edge.target;
               
                var dx = node2.layoutPosX - node1.layoutPosX;
                var dy = node2.layoutPosY - node1.layoutPosY;
                var d2 = dx * dx + dy * dy;
                if(d2 < 0.01) {
                        dx = getLayoutValue(3);
                        dy = getLayoutValue(4);
                        var d2 = dx * dx + dy * dy;
                }
                var d = Math.sqrt(d2);
                if(d > this.maxRepulsiveForceDistance) {
                        d = this.maxRepulsiveForceDistance;
                        d2 = d * d;
                }
                var attractiveForce = (d2 - this.k * this.k) / this.k;
                if(edge.weight == undefined || edge.weight < 1) edge.weight = 1;
                attractiveForce *= Math.log(edge.weight) * 0.5 + 1;
               
                node2.layoutForceX -= attractiveForce * dx / d;
                node2.layoutForceY -= attractiveForce * dy / d;
                node1.layoutForceX += attractiveForce * dx / d;
                node1.layoutForceY += attractiveForce * dy / d;
        }
};
