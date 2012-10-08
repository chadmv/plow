
---
--- Test Project
---
INSERT INTO plow.project VALUES ('00000000-0000-0000-0000-000000000000', null, 'test', 'The Test Project');
INSERT INTO plow.folder VALUES ('00000000-0000-0000-0000-000000000000', null, '00000000-0000-0000-0000-000000000000', 'All Jobs');
UPDATE plow.project SET pk_folder_default = '00000000-0000-0000-0000-000000000000' WHERE pk_project='00000000-0000-0000-0000-000000000000';

---
--- Test Cluster
--- 
INSERT INTO plow.cluster VALUES ('00000000-0000-0000-0000-000000000000', 'unassigned', 'unassigned', 'f');

