CREATE TABLE `ships` (
  `id` int(11) NOT NULL auto_increment,
  `owner` int(11) NOT NULL default '0',
  `name` varchar(50) NOT NULL default 'noname',
  `type` int(11) NOT NULL default '0',
  `modules` int(11),
  `cargo` text NOT NULL,
  `x` int(11) NOT NULL default '1',
  `y` int(11) NOT NULL default '1',
  `system` tinyint(4) NOT NULL default '1',
  `status` varchar(255) NOT NULL default '',
  `crew` int(11) unsigned NOT NULL default '0',
  `e` int(11) unsigned NOT NULL default '0',
  `s` int(11) unsigned NOT NULL default '0',
  `hull` int(11) unsigned NOT NULL default '1',
  `shields` int(11) unsigned NOT NULL default '0',
  `ablativeArmor` int(11) unsigned NOT NULL default '0',
  `heat` text NOT NULL,
  `engine` int(11) NOT NULL default '100',
  `weapons` int(11) NOT NULL default '100',
  `comm` int(11) NOT NULL default '100',
  `sensors` int(11) NOT NULL default '100',
  `docked` varchar(20) NOT NULL default '',
  `alarm` int(11) NOT NULL default '0',
  `fleet` int(11) default NULL,
  `destsystem` int(11) NOT NULL default '0',
  `destx` int(11) NOT NULL default '0',
  `desty` int(11) NOT NULL default '0',
  `destcom` text NOT NULL default '',
  `bookmark` tinyint(1) unsigned NOT NULL default '0',
  `battle` smallint(5) unsigned,
  `battleAction` tinyint(1) unsigned NOT NULL default '0',
  `jumptarget` varchar(100) NOT NULL default '',
  `autodeut` tinyint(3) unsigned NOT NULL default '1',
  `history` text NOT NULL,
  `oncommunicate` text,
  `lock` varchar(9) default NULL,
  `visibility` mediumint(9) default NULL,
  `onmove` text,
  `respawn` tinyint(4) default NULL,
  `startFighters` tinyint(3) unsigned NOT NULL default '1',
  `unitcargo` varchar (120),
  `version` int(10) unsigned not null default '0',
  `showtradepost` tinyint (4) NOT NULL default '0',
  `nahrungcargo` int(11) NOT NULL default '0',
  `isfeeding` tinyint(1) NOT NULL default '1',
  `isallyfeeding` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (`id`),
  KEY `coords` (`x`,`y`,`system`),
  KEY `owner` (`owner`,`id`),
  KEY `battle` (`battle`),
  KEY `status` (`status`),
  KEY `bookmark` (`bookmark`),
  KEY `type` (`type`),
  KEY `docked` (`docked`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 PACK_KEYS=0; 
