-- delete not used data
delete from `core_menu_i18n`;
delete from `core_menu`;
delete from `core_git`;
delete from `core_alarm`;

-- core_menu
insert into `core_menu`
(`menu_id`,`system_required`,`parent_menu_id`,`name`,`link`,`target`,`sort`,`icon`)
values
    ('simulate','Y',null,'Simulate','/simulate',null,1,'/static/image/icon-simulate.svg'),
    ('rank','Y',null,'Rank','/rank',null,2,'/static/image/icon-rank.svg');

-- core_menu_i18n
insert into `core_menu_i18n`
(`menu_id`,`language`,`name`)
values
    ('simulate','ko','시뮬레이션'),
    ('rank','ko','랭킹');
