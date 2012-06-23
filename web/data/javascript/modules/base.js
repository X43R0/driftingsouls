var Base = {
	highlightBuilding : function(buildingId) {
		$('#baseMap').addClass('fade');
		$('#baseMap .building'+buildingId).addClass('highlight');
	},
	noBuildingHighlight : function() {
		$('#baseMap').removeClass('fade');
		$('#baseMap .tile').removeClass('highlight');
	},
	changeName : function() {
		var el = $('#baseName');
		var name = el.text();
		el.empty();

		var cnt = DS.render(
				"<form action='{{URL}}' method='post' style='display:inline'>"+
				"<input name='newname' type='text' size='15' maxlength='50' value='{{basename}}' />"+
				"<input name='col' type='hidden' value='{{baseid}}' />"+
				"<input name='module' type='hidden' value='base' />"+
				"<input name='action' type='hidden' value='changeName' />"+
				"&nbsp;<input type='submit' value='umbenennen' />"+
				"</form>",
				{basename:name, baseid: this.getBaseId()}
		);

		el.append(cnt);

		$('#changename').css('display','none');
	},
	showBuilding : function(tileId) {
		new BuildingUi(this, tileId);
	},

	refreshAll : function() {
		var self = this;
		DS.get(
				{module:'base', col:this.getBaseId()},
				function(resp) {self.__parseRefreshAll(resp)}
		);
	},
	__parseRefreshAll : function(resp) {
		var response = $(resp);
		var cnt = response.filter('#baseContent');
		$('#baseContent').replaceWith(cnt);
	},

	refreshCargoAndActions : function() {
		var self = this;
		DS.get(
				{module:'base', col:this.getBaseId()},
				function(resp) {self.__parseRefreshCargoAndActions(resp)}
		);
	},
	__parseRefreshCargoAndActions : function(resp) {
		var newCargoBox = $(resp).find('#cargoBox');
		$('#cargoBox').replaceWith(newCargoBox);

		var newActionBox = $(resp).find('.buildingActions');
		$('.buildingActions').replaceWith(newActionBox);

		var newStatsBox = $(resp).find('#statsBox');
		$('#statsBox').replaceWith(newStatsBox);
	},
	getBaseId : function() {
		return $('#baseId').val();
	}
};

function BuildingUi(base, tileId) {
	var __defaultBuildingHandler = {
		generateOutput : function(resp) {
			var tmpl =
				'<dl class="defaultBuilding">'+
				'<dt>Verbraucht:</dt>'+
				"<dd>"+
				'{{#consumes}}'+
				'{{#cargo}}'+
				'<img src="{{image}}" alt="" title="{{plainname}}" />{{{cargo1}}} '+
				'{{/cargo}}'+
				'{{#energy}}'+
				'<img src="./data/interface/energie.gif" alt="" title="Energie" />{{count}} '+
				'{{/energy}}'+
				'{{/consumes}}'+
				'{{^consumes}}-{{/consumes}}'+
				"</dd>"+

				"<dt>Produziert:</dt>"+
				"<dd>"+
				'{{#produces}}'+
				'{{#cargo}}'+
				'<img src="{{image}}" alt="" title="{{plainname}}" />{{{cargo1}}} '+
				'{{/cargo}}'+
				'{{#energy}}'+
				'<img src="./data/interface/energie.gif" alt="" title="Energie" />{{count}} '+
				'{{/energy}}'+
				'{{/produces}}'+
				'{{^produces}}-{{/produces}}'+
				"</dd>"+
				'</dl>';

			return DS.render(tmpl, resp.buildingUI);
		}
	};

	function BuildingView() {
		function createViewBox() {
			if( $('#buildingBox').size() == 0 ) {
				$('body').append('<div id="buildingBox" />');
				$('#buildingBox').dsBox({
					draggable:true,
					center:true
				});
			}
		};

		function renderEmpty() {
			if( $('#buildingBox').size() == 0 ) {
				$('body').append('<div id="buildingBox" />');
				$('#buildingBox').dsBox({
					draggable:true,
					center:true
				});
			}

			var buildingBox = $('#buildingBox');
			buildingBox.dsBox('show');
			var content = buildingBox.find('.content');
			content.empty();
			content.append('Lade...');
		};

		function renderBuilding(model) {
			var content = $('#buildingBox .content');
			content.empty();

			var tmpl = '<div class="head">'+
				'<img src="./{{building.picture}}" alt="" /> {{building.name}}'+
				'</div>'+
				'<div class="message" />'+
				'<div class="status">Status: '+
				'{{#building.active}}<span class="aktiv">Aktiv</span>{{/building.active}}'+
				'{{^building.active}}<span class="inaktiv">Inaktiv</span>{{/building.active}}'+
				'</div>'+
				'{{{ui}}}'+
				'<ul class="aktionen">Aktionen: {{#building.deakable}}'+
				'{{#building.active}}'+
				'<li><a id="startStopBuilding" href="#">deaktivieren</a></li>'+
				'{{/building.active}}'+
				'{{^building.active}}'+
				'<li><a id="startStopBuilding" href="#">aktivieren</a></li>'+
				'{{/building.active}}'+
				'{{/building.deakable}}'+
				'{{^building.kommandozentrale}}'+
				'<li><a class="error" id="demoBuilding" href="#">abreissen</a></li>'+
				'{{/building.kommandozentrale}}'+
				'{{#building.kommandozentrale}}'+
				'<li><a class="error" id="demoBuilding" href="{{URL}}?module=building&action=demo&col={{col}}&field={{field}}&conf=ok">Asteroid aufgeben</a></li>'+
				'{{/building.kommandozentrale}}'+
				"</ul>";

			var buildingUi;
			// BUILDING
			if( model.building.type === 'DefaultBuilding' ) {
				buildingUi = __defaultBuildingHandler.generateOutput(model);
			}
			else {
				buildingUi = "Unbekannter Gebäudetyp: "+model.building.type;
			}

			model.ui = buildingUi;

			var out = DS.render(tmpl, model);

			content.append(out);
			DsTooltip.update(content);
		};

		function doShutdown() {
			$('#startStopBuilding').text('aktivieren');
			$('#buildingBox .status .aktiv').remove();
			$('#buildingBox .status').append('<span class="inaktiv">Inaktiv</span>');
		}

		function doStartup() {
			$('#startStopBuilding').text('deaktivieren');
			$('#buildingBox .status .inaktiv').remove();
			$('#buildingBox .status').append('<span class="aktiv">Aktiv</span>');
		}

		function renderMessage(message) {
			$('#buildingBox .message')
				.empty()
				.append(message);
		}

		function renderAskDemo(model) {
			var content = $('#buildingBox .content');
			content.empty();

			var tmpl = '<div class="head">'+
				'<img src="./{{building.picture}}" alt="" /> {{building.name}}'+
				'</div>'+
				'<div class="message">Wollen sie dieses Gebäude wirklich abreissen?</div>'+
				'<ul class="confirm">'+
				'<li><a id="cancelDemo" href="#">abbrechen</a></li>'+
				'<li><a class="error" id="okDemo" href="#">abreissen</a></li>'+
				'</ul>';

			var out = DS.render(tmpl, model);

			content.append(out);
			DsTooltip.update(content);
		}

		function renderDemoResponse(demoModel) {
			tmpl = '<div align="center">Rückerstattung:</div><br />'+
				'{{#demoCargo}}'+
				'<img src="{{image}}" alt="" />{{{cargo1}}}'+
				'{{#spaceMissing}} - <span style="color:red">Nicht genug Platz für alle Waren</span>{{/spaceMissing}}'+
				'<br />'+
				'{{/demoCargo}}'+
				'<br />'+
				'<hr noshade="noshade" size="1" style="color:#cccccc" /><br />'+
				'<div align="center"><span style="color:#ff0000">Das Gebäude wurde demontiert</span></div>';

			var content = $('#buildingBox .content');
			content.empty();

			var out = DS.render(tmpl, demoModel);

			content.append(out);
			DsTooltip.update(content);
		}

		createViewBox();

		// Public Methods
		this.renderEmpty = renderEmpty;
		this.renderBuilding = renderBuilding;
		this.doShutdown = doShutdown;
		this.doStartup = doStartup;
		this.renderMessage = renderMessage;
		this.renderAskDemo = renderAskDemo;
		this.renderDemoResponse = renderDemoResponse;
	};

	var view = new BuildingView();

	function showAskDemo(model) {
		view.renderAskDemo(model);

		$('#cancelDemo').bind('click', function() {
			__parseBuildingResponse(model);
		});
		$('#okDemo').bind('click', function() {
			DS.getJSON(
				{module:'building', action:'demoAjax', 'col':model.col, 'field':model.field},
				parseDemoBuilding
			);
			return false;
		});
	};

	function parseDemoBuilding(demoModel) {
		view.renderDemoResponse(demoModel);

		base.refreshAll();
	};

	function __parseBuildingResponse(model) {
		if( model.noJsonSupport ) {
			document.location.href=DS.getUrl()+'?module=building&action=default&col='+model.col+'&field='+model.field;
			return;
		}

		view.renderBuilding(model);

		if( model.building.kommandozentrale ) {
			$('#demoBuilding').bind('click.demoBuilding', function() {
				return confirm('Wollen sie den Asteroiden wirklich aufgeben?');
			});
		}
		else {
			$('#demoBuilding').bind('click.demoBuilding', function() {
				showAskDemo(model);
			});
		}

		__bindBuildingStartStop(model.col, model.field, model.building.active);
	};

	function __bindBuildingStartStop(col, field, active) {
		$('#startStopBuilding').unbind('click.startStop');

		if( active ) {
			$('#startStopBuilding').bind('click.startStop', function() {
				DS.getJSON(
					{module:'building', action:'shutdownAjax', 'col':col, 'field':field},
					__parseBuildingShutdown
				);
				return false;
			});
		}
		else {
			$('#startStopBuilding').bind('click.startStop', function() {
				DS.getJSON(
					{module:'building', action:'startAjax', 'col':col, 'field':field},
					__parseBuildingStart
				);
				return false;
			});
		}
	};
	function __parseBuildingShutdown(resp) {
		if( !resp.success ) {
			view.renderMessage(resp.message);

			return;
		}

		var link = $('#baseMap .p'+resp.field).html();

		$('#baseMap').append('<div class="o'+resp.field+' overlay">'+link+'</div>');
		$('#baseMap .o'+resp.field+' img').attr('src','./data/buildings/overlay_offline.png');

		view.doShutdown();

		__bindBuildingStartStop(resp.col, resp.field, false);

		base.refreshCargoAndActions();
	};
	function __parseBuildingStart(resp) {
		if( !resp.success ) {
			view.renderMessage(resp.message);

			return;
		}

		$('#baseMap .o'+resp.field).remove();

		view.doStartup();

		__bindBuildingStartStop(resp.col, resp.field, true);

		base.refreshCargoAndActions();
	};

	view.renderEmpty();

	DS.getJSON({
		module:'building',
		action:'ajax',
		col:base.getBaseId(),
		field:tileId
	}, __parseBuildingResponse);
};