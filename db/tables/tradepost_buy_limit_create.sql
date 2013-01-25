CREATE TABLE `tradepost_buy_limit` (
  `shipid` int(11) NOT NULL,
  `resourceid` int(11) NOT NULL,
  `maximum` int(11) NOT NULL,
  `min_rank` INT NOT NULL DEFAULT  '0',
  `version` int(10) unsigned not null default '0',
  PRIMARY KEY  (`shipid`,`resourceid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
