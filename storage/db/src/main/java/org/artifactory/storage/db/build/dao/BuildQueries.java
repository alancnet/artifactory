package org.artifactory.storage.db.build.dao;

/**
 * @author Chen Keinan
 */
public class BuildQueries {

        public final static String MODULE_DEPENDENCY_DIFF_QUERY = "select * from (\n" +
                // get module dependencies which are in build b and not in a - removed from last build
            "SELECT * from (\n" +
                "                SELECT  build_dependencies.dependency_name_id ,build_dependencies.dependency_type,build_dependencies.sha1  sh,build_dependencies.dependency_scopes,'Removed'  status FROM build_dependencies\n" +
                "                                left join build_modules on build_modules.module_id=build_dependencies.module_id\n" +
                "                                left join builds on  build_modules.build_id = builds.build_id\n" +
                "                              where build_dependencies.dependency_name_id not in (SELECT distinct build_dependencies.dependency_name_id   FROM build_dependencies\n" +
                "                                left join build_modules on build_modules.module_id=build_dependencies.module_id\n" +
                "                                left join builds on  build_modules.build_id = builds.build_id\n" +
                "                              where builds.build_number = ? and builds.build_date = ?  and\n" +
                "                                    build_modules.module_name_id = ?) and builds.build_number = ? and builds.build_date = ? and\n" +
                "                                    build_modules.module_name_id = ?\n" +
            "\n" +
                // get module dependencies which are in build a and not in b - added to last build
                "union\n" +
                "        SELECT  build_dependencies.dependency_name_id ,build_dependencies.dependency_type,build_dependencies.sha1  sh,build_dependencies.dependency_scopes,'New' status FROM build_dependencies\n" +
                "                        left join build_modules on build_modules.module_id=build_dependencies.module_id\n" +
                "                        left join builds on  build_modules.build_id = builds.build_id\n" +
                "                      where build_dependencies.dependency_name_id not in (SELECT distinct build_dependencies.dependency_name_id   FROM build_dependencies\n" +
                "                        left join build_modules on build_modules.module_id=build_dependencies.module_id\n" +
                "                        left join builds on  build_modules.build_id = builds.build_id\n" +
                "                      where builds.build_number = ? and builds.build_date =?  and\n" +
                "                            build_modules.module_name_id = ?) and builds.build_number = ? and builds.build_date = ?  and\n" +
                "                            build_modules.module_name_id = ?)  x\n" +
            "\n" +
            "union\n" +
                // get module dependencies which has the same sha1 on both builds - no changes
                "        SELECT o.c,o.dependency_type,o.sh,o.dependency_scopes,o.status from (\n" +
                "                 SELECT * from (\n" +
                "                                 SELECT  build_dependencies.dependency_name_id c  ,build_dependencies.dependency_type ,build_dependencies.sha1 sh,build_dependencies.dependency_scopes,'Unchanged' status1 FROM build_dependencies\n" +
                "                                   left join build_modules on build_modules.module_id=build_dependencies.module_id\n" +
                "                                   left join builds on  build_modules.build_id = builds.build_id\n" +
                "                                 where builds.build_number = ? and builds.build_date = ? and build_modules.module_name_id = ? )  x\n" +
                "                   inner join (\n" +
            "\n" +
                "                 SELECT * from (\n" +
                "                                SELECT  build_dependencies.dependency_name_id  b,build_dependencies.dependency_type type_x,build_dependencies.sha1 sh2,build_dependencies.dependency_scopes scope,'Unchanged' status FROM build_dependencies\n" +
                "                                  left join build_modules on build_modules.module_id=build_dependencies.module_id\n" +
                "                                  left join builds on  build_modules.build_id = builds.build_id\n" +
                "                                where builds.build_number = ? and builds.build_date = ? and\n" +
                "                                      build_modules.module_name_id = ?)  t)  v on v.sh2 = x.sh) o\n" +
            "\n" +
                // get module dependencies which has the diff sha1 on both builds - has changes
                "union\n" +
                "        SELECT o.c,o.dependency_type,o.sh,o.dependency_scopes,o.status from (\n" +
                "             SELECT * from (\n" +
                "                             SELECT  build_dependencies.dependency_name_id  c  ,build_dependencies.dependency_type ,build_dependencies.sha1 sh,build_dependencies.dependency_scopes,'Updated' status1 FROM build_dependencies\n" +
                "                               left join build_modules on build_modules.module_id=build_dependencies.module_id\n" +
                "                               left join builds on  build_modules.build_id = builds.build_id\n" +
                "                             where builds.build_number = ? and builds.build_date = ? and build_modules.module_name_id = ? )  x\n" +
            "\n" +
                "               inner join (\n" +
            "\n" +
                "             SELECT * from (\n" +
                "                            SELECT  build_dependencies.dependency_name_id  b,build_dependencies.dependency_type type_x,build_dependencies.sha1 sh2,build_dependencies.dependency_scopes  scop,'Updated' status FROM build_dependencies\n" +
                "                              left join build_modules on build_modules.module_id=build_dependencies.module_id\n" +
                "                              left join builds on  build_modules.build_id = builds.build_id\n" +
                "                            where builds.build_number = ? and builds.build_date = ? and\n" +
                "                                  build_modules.module_name_id = ?)  t) v on v.b = x.c where v.sh2 != x.sh) o\n" +
                "                                  )  z  ";
        public final static String MODULE_ARTIFACT_DIFF_QUERY = "\n" +
            "select * from (\n" +
                // get module artifacts which are in build b and not in a - removed from last build
                "\n" +
            "                SELECT * from (\n" +
                "                                SELECT  build_artifacts.artifact_name ,build_artifacts.artifact_type,build_artifacts.sha1  sh,'Removed' status FROM build_artifacts\n" +
            "                                  left join build_modules on build_modules.module_id=build_artifacts.module_id\n" +
            "                                  left join builds on  build_modules.build_id = builds.build_id\n" +
            "                                where build_artifacts.artifact_name not in (SELECT distinct build_artifacts.artifact_name   FROM build_artifacts\n" +
            "                                  left join build_modules on build_modules.module_id=build_artifacts.module_id\n" +
            "                                  left join builds on  build_modules.build_id = builds.build_id\n" +
            "                                where builds.build_number = ? and builds.build_date = ?  and\n" +
            "                                      build_modules.module_name_id = ?) and builds.build_number = ? and builds.build_date = ? and\n" +
            "                                      build_modules.module_name_id = ?\n" +
                // get module artifacts which are in build a and not in b - added to last build
                "\n" +
            "                                union\n" +
                "                                SELECT  build_artifacts.artifact_name ,build_artifacts.artifact_type,build_artifacts.sha1  sh,'New'  status FROM build_artifacts\n" +
            "                                  left join build_modules on build_modules.module_id=build_artifacts.module_id\n" +
            "                                  left join builds on  build_modules.build_id = builds.build_id\n" +
            "                                where build_artifacts.artifact_name not in (SELECT distinct build_artifacts.artifact_name   FROM build_artifacts\n" +
            "                                  left join build_modules on build_modules.module_id=build_artifacts.module_id\n" +
            "                                  left join builds on  build_modules.build_id = builds.build_id\n" +
            "                                where builds.build_number = ? and builds.build_date =?  and\n" +
            "                                      build_modules.module_name_id = ?) and builds.build_number = ? and builds.build_date = ?  and\n" +
                "                                      build_modules.module_name_id = ?)  x\n" +
            "\n" +
                // get module artifacts which has the same sha1 on both builds - no changes
                "                union\n" +
                "                SELECT o.c,o.artifact_type,o.sh,o.status from (\n" +
            "                                                       SELECT * from (\n" +
                "                                                                       SELECT  build_artifacts.artifact_name  c  ,build_artifacts.artifact_type ,build_artifacts.sha1  sh,'Unchanged'  status1 FROM build_artifacts\n" +
            "                                                                         left join build_modules on build_modules.module_id=build_artifacts.module_id\n" +
            "                                                                         left join builds on  build_modules.build_id = builds.build_id\n" +
            "                                                                       where builds.build_number = ? and builds.build_date = ? and build_modules.module_name_id = ? )  x\n" +
            "                                                         inner join (\n" +
            "\n" +
            "                                                                      SELECT * from (\n" +
                "                                                                                      SELECT  build_artifacts.artifact_name  b,build_artifacts.artifact_type  type_x,build_artifacts.sha1  sh2,'Unchanged'  status FROM build_artifacts\n" +
            "                                                                                        left join build_modules on build_modules.module_id=build_artifacts.module_id\n" +
            "                                                                                        left join builds on  build_modules.build_id = builds.build_id\n" +
            "                                                                                      where builds.build_number = ? and builds.build_date = ? and\n" +
                "                                                                                            build_modules.module_name_id = ?)  t)  v on v.sh2 = x.sh) o\n" +
            "\n" +
                // get module artifacts which has the diff sha1 on both builds - has changes
                "                union\n" +
                "                SELECT o.c,o.artifact_type,o.sh,o.status from (\n" +
            "                                                       SELECT * from (\n" +
                "                                                                       SELECT  build_artifacts.artifact_name  c  ,build_artifacts.artifact_type ,build_artifacts.sha1  sh,'Updated'  status1 FROM build_artifacts\n" +
            "                                                                         left join build_modules on build_modules.module_id=build_artifacts.module_id\n" +
            "                                                                         left join builds on  build_modules.build_id = builds.build_id\n" +
            "                                                                       where builds.build_number = ? and builds.build_date = ? and build_modules.module_name_id = ? )  x\n" +
            "\n" +
            "                                                         inner join (\n" +
            "\n" +
            "                                                                      SELECT * from (\n" +
                "                                                                                      SELECT  build_artifacts.artifact_name  b,build_artifacts.artifact_type  type_x,build_artifacts.sha1  sh2,'Updated'  status FROM build_artifacts\n" +
            "                                                                                        left join build_modules on build_modules.module_id=build_artifacts.module_id\n" +
            "                                                                                        left join builds on  build_modules.build_id = builds.build_id\n" +
            "                                                                                      where builds.build_number = ? and builds.build_date = ? and\n" +
                "                                                                                            build_modules.module_name_id = ?)  t)  v on v.b = x.c where v.sh2 != x.sh) o )  z  \n" +
            "\n";

        public final static String MODULE_DEPENDENCY_DIFF_COUNT = "  SELECT count(*) as cnt from (\n" +
            "                SELECT  build_dependencies.dependency_name_id ,build_dependencies.dependency_type,build_dependencies.sha1 FROM build_dependencies\n" +
            "                  left join build_modules on build_modules.module_id=build_dependencies.module_id\n" +
            "                  left join builds on  build_modules.build_id = builds.build_id\n" +
            "                where builds.build_number = ? and builds.build_date = ? and\n" +
            "                      build_modules.module_name_id = ?\n" +
            "                union\n" +
            "\t\t\n" +
            "  SELECT  build_dependencies.dependency_name_id ,build_dependencies.dependency_type,build_dependencies.sha1 FROM build_dependencies\n" +
            "                  left join build_modules on build_modules.module_id=build_dependencies.module_id\n" +
            "                  left join builds on  build_modules.build_id = builds.build_id\n" +
            "                where build_dependencies.dependency_name_id not in (SELECT distinct build_dependencies.dependency_name_id   FROM build_dependencies\n" +
            "                  left join build_modules on build_modules.module_id=build_dependencies.module_id\n" +
            "                  left join builds on  build_modules.build_id = builds.build_id\n" +
            "                where builds.build_number = ? and builds.build_date =?  and\n" +
            "                      build_modules.module_name_id = ?) and builds.build_number = ? and builds.build_date = ?  and\n" +
            "                      build_modules.module_name_id = ?) as x";


        public final static String MODULE_ARTIFACT_DIFF_COUNT = "  SELECT count(*) as cnt from (\n" +
            "                SELECT  build_artifacts.artifact_name ,build_artifacts.artifact_type,build_artifacts.sha1 FROM build_artifacts\n" +
            "                  left join build_modules on build_modules.module_id=build_artifacts.module_id\n" +
            "                  left join builds on  build_modules.build_id = builds.build_id\n" +
            "                where builds.build_number = ? and builds.build_date = ? and\n" +
            "                      build_modules.module_name_id = ?\n" +
            "                union\n" +
            "\t\t\n" +
            "  SELECT  build_artifacts.artifact_name ,build_artifacts.artifact_type,build_artifacts.sha1 FROM build_artifacts\n" +
            "                  left join build_modules on build_modules.module_id=build_artifacts.module_id\n" +
            "                  left join builds on  build_modules.build_id = builds.build_id\n" +
            "                where build_artifacts.artifact_name not in (SELECT distinct build_artifacts.artifact_name   FROM build_artifacts\n" +
            "                  left join build_modules on build_modules.module_id=build_artifacts.module_id\n" +
            "                  left join builds on  build_modules.build_id = builds.build_id\n" +
            "                where builds.build_number = ? and builds.build_date =?  and\n" +
            "                      build_modules.module_name_id = ?) and builds.build_number = ? and builds.build_date = ?  and\n" +
            "                      build_modules.module_name_id = ?) as x";

    public final static String BUILD_ARTIFACT_DIFF_QUERY = "\t\t\t\tselect i.b,i.type,i.sh2,i.st,i.m1 from (\n" +
            "\t\t\t\tselect * from (\n" +
            "\t\t\t\t  SELECT  build_artifacts.artifact_name ,build_artifacts.artifact_type,build_artifacts.sha1,'Removed' as status,build_modules.module_name_id as m FROM build_artifacts\n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t  left join build_modules on build_modules.module_id=build_artifacts.module_id\n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t  left join builds on  build_modules.build_id = builds.build_id\n" +
            "\t\t\t\t\t\t\t\t\t\t\t\twhere  builds.build_number = ? and builds.build_date = ?) u\n" +
            "\n" +
            "\n" +
            "\t\t\t\tright join (\n" +
            "\t\t\t\tSELECT * from (\n" +
            "\t\t\t\t\t   SELECT  build_artifacts.artifact_name as b,build_artifacts.artifact_type as type,build_artifacts.sha1 as sh2,'Removed' as st,build_modules.module_name_id as m1 FROM build_artifacts\n" +
            "\t\t\t\t\t\t left join build_modules on build_modules.module_id=build_artifacts.module_id\n" +
            "\t\t\t\t\t\t left join builds on  build_modules.build_id = builds.build_id\n" +
            "\t\t\t\t\t   where builds.build_number = ? and builds.build_date = ?)  t) as v on v.b = u.artifact_name where u.artifact_name is null)as i\n" +
            "\n" +
            "                union\n" +
            "                \n" +
            "                select i.b,i.type,i.sh2,i.st,i.m1 from (\n" +
            "\t\t\t\tselect * from (\n" +
            "\t\t\t\t  SELECT  build_artifacts.artifact_name ,build_artifacts.artifact_type,build_artifacts.sha1,'Add' as status,build_modules.module_name_id as m FROM build_artifacts\n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t  left join build_modules on build_modules.module_id=build_artifacts.module_id\n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t  left join builds on  build_modules.build_id = builds.build_id\n" +
            "\t\t\t\t\t\t\t\t\t\t\t\twhere  builds.build_number = ? and builds.build_date = ?) u\n" +
            "\n" +
            "\n" +
            "\t\t\t\tright join (\n" +
            "\t\t\t\tSELECT * from (\n" +
            "\t\t\t\t\t   SELECT  build_artifacts.artifact_name as b,build_artifacts.artifact_type as type,build_artifacts.sha1 as sh2,'Add' as st,build_modules.module_name_id as m1 FROM build_artifacts\n" +
            "\t\t\t\t\t\t left join build_modules on build_modules.module_id=build_artifacts.module_id\n" +
            "\t\t\t\t\t\t left join builds on  build_modules.build_id = builds.build_id\n" +
            "\t\t\t\t\t   where builds.build_number = ? and builds.build_date = ?)  t) as v on v.b = u.artifact_name where u.artifact_name is null)as i\n" +
            "\n" +
            "                union\n" +
            "                SELECT o.c,o.artifact_type,o.sh,o.status,o.m from (\n" +
            "\t\t\t\t\tSELECT * from (\n" +
            "\t\t\t\t\t\t\t\t\tSELECT  build_artifacts.artifact_name as c  ,build_artifacts.artifact_type ,build_artifacts.sha1 as sh,'Unchanged' as status1,build_modules.module_name_id as m FROM build_artifacts\n" +
            "\t\t\t\t\t\t\t\t\t  left join build_modules on build_modules.module_id=build_artifacts.module_id\n" +
            "\t\t\t\t\t\t\t\t\t  left join builds on  build_modules.build_id = builds.build_id\n" +
            "\t\t\t\t\t\t\t\t\twhere builds.build_number = ? and builds.build_date = ? )  x\n" +
            "\t\t\t\t\t  inner join (\n" +
            "\n" +
            "\t\t\t\t\t\t\t\t   SELECT * from (\n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t   SELECT  build_artifacts.artifact_name as b,build_artifacts.artifact_type as type,build_artifacts.sha1 as sh2,'Unchanged' as status,build_modules.module_name_id as m1 FROM build_artifacts\n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t\t left join build_modules on build_modules.module_id=build_artifacts.module_id\n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t\t left join builds on  build_modules.build_id = builds.build_id\n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t   where builds.build_number = ? and builds.build_date = ?)  t) as v on v.sh2 = x.sh) o\n" +
            "\n" +
            "                union\n" +
            "                SELECT o.c,o.artifact_type,o.sh,o.status,o.m from (\n" +
            "\t\t\t\t\tSELECT * from (\n" +
            "\t\t\t\t\t\t\t\t\tSELECT  build_artifacts.artifact_name as c  ,build_artifacts.artifact_type ,build_artifacts.sha1 as sh,'Updated' as status1,build_modules.module_name_id as m FROM build_artifacts\n" +
            "\t\t\t\t\t\t\t\t\t  left join build_modules on build_modules.module_id=build_artifacts.module_id\n" +
            "\t\t\t\t\t\t\t\t\t  left join builds on  build_modules.build_id = builds.build_id\n" +
            "\t\t\t\t\t\t\t\t\twhere builds.build_number = ? and builds.build_date = ? )  x\n" +
            "\n" +
            "\t\t\t\t\t  inner join (\n" +
            "\n" +
            "\t\t\t\t\t\t\t\t   SELECT * from (\n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t   SELECT  build_artifacts.artifact_name as b,build_artifacts.artifact_type as type,build_artifacts.sha1 as sh2,'Updated' as status,build_modules.module_name_id as m1 FROM build_artifacts\n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t\t left join build_modules on build_modules.module_id=build_artifacts.module_id\n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t\t left join builds on  build_modules.build_id = builds.build_id\n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t   where builds.build_number = ? and builds.build_date = ?)  t) as v on v.b = x.c where v.sh2 != x.sh) o\n" +
            "\n";


    public final static String BUILD_DEPENDENCY_DIFF_QUERY = " select * from (\n" +
            "\t\t\t\tselect i.c,i.type,i.sh2,i.sc,i.st,i.m1 from (\n" +
            "\t\t\t\tselect * from (\n" +
            "\t\t\t\t  SELECT  build_dependencies.dependency_name_id ,build_dependencies.dependency_type,build_dependencies.sha1,build_dependencies.dependency_scopes,'Removed' as status,build_modules.module_name_id as m FROM build_dependencies\n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t  left join build_modules on build_modules.module_id=build_dependencies.module_id\n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t  left join builds on  build_modules.build_id = builds.build_id\n" +
            "\t\t\t\t\t\t\t\t\t\t\t\twhere  builds.build_number = ? and builds.build_date = ?) u\n" +
            "\n" +
            "\n" +
            "\t\t\t\tright join (\n" +
            "\t\t\t\tSELECT * from (\n" +
            "\t\t\t\t\t   SELECT  build_dependencies.dependency_name_id as c,build_dependencies.dependency_type as type,build_dependencies.sha1 as sh2,build_dependencies.dependency_scopes as sc,'Removed' as st,build_modules.module_name_id as m1 FROM build_dependencies\n" +
            "\t\t\t\t\t\t left join build_modules on build_modules.module_id=build_dependencies.module_id\n" +
            "\t\t\t\t\t\t left join builds on  build_modules.build_id = builds.build_id\n" +
            "\t\t\t\t\t   where builds.build_number = ? and builds.build_date = ?)  t) as v on v.c = u.dependency_name_id where u.dependency_name_id is null)as i\n" +
            "\n" +
            "                union\n" +
            "                \n" +
            "                select i.c,i.type,i.sh2,i.sc,i.st,i.m1 from (\n" +
            "\t\t\t\tselect * from (\n" +
            "\t\t\t\t  SELECT  build_dependencies.dependency_name_id ,build_dependencies.dependency_type,build_dependencies.sha1,build_dependencies.dependency_scopes,'Add' as status,build_modules.module_name_id as m FROM build_dependencies\n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t  left join build_modules on build_modules.module_id=build_dependencies.module_id\n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t  left join builds on  build_modules.build_id = builds.build_id\n" +
            "\t\t\t\t\t\t\t\t\t\t\t\twhere  builds.build_number = ? and builds.build_date = ?) u\n" +
            "\n" +
            "\n" +
            "\t\t\t\tright join (\n" +
            "\t\t\t\tSELECT * from (\n" +
            "\t\t\t\t\t   SELECT  build_dependencies.dependency_name_id as c,build_dependencies.dependency_type as type,build_dependencies.sha1 as sh2,build_dependencies.dependency_scopes as sc,'Add' as st,build_modules.module_name_id as m1 FROM build_dependencies\n" +
            "\t\t\t\t\t\t left join build_modules on build_modules.module_id=build_dependencies.module_id\n" +
            "\t\t\t\t\t\t left join builds on  build_modules.build_id = builds.build_id\n" +
            "\t\t\t\t\t   where builds.build_number = ? and builds.build_date = ?)  t) as v on v.c = u.dependency_name_id where u.dependency_name_id is null)as i\n" +
            "\n" +
            "                union\n" +
            "                SELECT o.c,o.dependency_type,o.sh,o.dependency_scopes,o.status,o.m from (\n" +
            "\t\t\t\t\tSELECT * from (\n" +
            "\t\t\t\t\t\t\t\t\tSELECT  build_dependencies.dependency_name_id as c  ,build_dependencies.dependency_type ,build_dependencies.sha1 as sh,build_dependencies.dependency_scopes,'Unchanged' as status1,build_modules.module_name_id as m FROM build_dependencies\n" +
            "\t\t\t\t\t\t\t\t\t  left join build_modules on build_modules.module_id=build_dependencies.module_id\n" +
            "\t\t\t\t\t\t\t\t\t  left join builds on  build_modules.build_id = builds.build_id\n" +
            "\t\t\t\t\t\t\t\t\twhere builds.build_number = ? and builds.build_date = ? )  x\n" +
            "\t\t\t\t\t  inner join (\n" +
            "\n" +
            "\t\t\t\t\t\t\t\t   SELECT * from (\n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t   SELECT  build_dependencies.dependency_name_id as b,build_dependencies.dependency_type as type,build_dependencies.sha1 as sh2,build_dependencies.dependency_scopes as sc,'Unchanged' as status,build_modules.module_name_id as m1 FROM build_dependencies\n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t\t left join build_modules on build_modules.module_id=build_dependencies.module_id\n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t\t left join builds on  build_modules.build_id = builds.build_id\n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t   where builds.build_number = ? and builds.build_date = ?)  t) as v on v.sh2 = x.sh) o\n" +
            "\n" +
            "                union\n" +
            "                SELECT o.c,o.dependency_type,o.sh,o.dependency_scopes,o.status,o.m from (\n" +
            "\t\t\t\t\tSELECT * from (\n" +
            "\t\t\t\t\t\t\t\t\tSELECT  build_dependencies.dependency_name_id as c  ,build_dependencies.dependency_type ,build_dependencies.sha1 as sh,build_dependencies.dependency_scopes,'Updated' as status1,build_modules.module_name_id as m FROM build_dependencies\n" +
            "\t\t\t\t\t\t\t\t\t  left join build_modules on build_modules.module_id=build_dependencies.module_id\n" +
            "\t\t\t\t\t\t\t\t\t  left join builds on  build_modules.build_id = builds.build_id\n" +
            "\t\t\t\t\t\t\t\t\twhere builds.build_number = ? and builds.build_date = ? )  x\n" +
            "\n" +
            "\t\t\t\t\t  inner join (\n" +
            "\n" +
            "\t\t\t\t\t\t\t\t   SELECT * from (\n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t   SELECT  build_dependencies.dependency_name_id as b,build_dependencies.dependency_type as type,build_dependencies.sha1 as sh2,build_dependencies.dependency_scopes as sc,'Updated' as status,build_modules.module_name_id as m1 FROM build_dependencies\n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t\t left join build_modules on build_modules.module_id=build_dependencies.module_id\n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t\t left join builds on  build_modules.build_id = builds.build_id\n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t   where builds.build_number = ? and builds.build_date = ?)  t) as v on v.b = x.c where v.sh2 != x.sh) o) x ";

    public final static String BUILD_ARTIFACT_DIFF_COUNT = "  SELECT count(*) as cnt from (\n" +
            "                SELECT  build_artifacts.artifact_name ,build_artifacts.artifact_type,build_artifacts.sha1 FROM build_artifacts\n" +
            "                  left join build_modules on build_modules.module_id=build_artifacts.module_id\n" +
            "                  left join builds on  build_modules.build_id = builds.build_id\n" +
            "                where builds.build_number = ? and builds.build_date = ? \n" +
            "                union\n" +
            "\t\t\n" +
            "  SELECT  build_artifacts.artifact_name ,build_artifacts.artifact_type,build_artifacts.sha1 FROM build_artifacts\n" +
            "                  left join build_modules on build_modules.module_id=build_artifacts.module_id\n" +
            "                  left join builds on  build_modules.build_id = builds.build_id\n" +
            "                where build_artifacts.artifact_name not in (SELECT distinct build_artifacts.artifact_name   FROM build_artifacts\n" +
            "                  left join build_modules on build_modules.module_id=build_artifacts.module_id\n" +
            "                  left join builds on  build_modules.build_id = builds.build_id\n" +
            "                where builds.build_number = ? and builds.build_date =? ) and builds.build_number = ? and builds.build_date = ?  \n" +
            "                      ) as x";

    public final static String BUILD_DEPENDENCY_DIFF_COUNT = "  SELECT count(*) as cnt from (\n" +
            "                SELECT  build_dependencies.dependency_name_id ,build_dependencies.dependency_type,build_dependencies.sha1 FROM build_dependencies\n" +
            "                  left join build_modules on build_modules.module_id=build_dependencies.module_id\n" +
            "                  left join builds on  build_modules.build_id = builds.build_id\n" +
            "                where builds.build_number = ? and builds.build_date = ? " +
            "                union\n" +
            "\t\t\n" +
            "  SELECT  build_dependencies.dependency_name_id ,build_dependencies.dependency_type,build_dependencies.sha1 FROM build_dependencies\n" +
            "                  left join build_modules on build_modules.module_id=build_dependencies.module_id\n" +
            "                  left join builds on  build_modules.build_id = builds.build_id\n" +
            "                where build_dependencies.dependency_name_id not in (SELECT distinct build_dependencies.dependency_name_id   FROM build_dependencies\n" +
            "                  left join build_modules on build_modules.module_id=build_dependencies.module_id\n" +
            "                  left join builds on  build_modules.build_id = builds.build_id\n" +
            "                where builds.build_number = ? and builds.build_date =? ) and builds.build_number = ? and builds.build_date = ? ) as x ";

    public final static String BUILD_PROPS_DIFF = "select * from (\tSELECT distinct build_props.prop_key,build_props.prop_value,'New' status,'new' new from  build_props inner join\n" +
            "  builds on builds.build_id = build_props.build_id where build_props.prop_key not in\n" +
            "                                                         (SELECT distinct build_props.prop_key from  build_props inner join\n" +
            "                                                           builds on builds.build_id = build_props.build_id where builds.build_number = ? and builds.build_date = ?)  and\n" +
            "                                                         builds.build_number = ? and builds.build_date = ?\n" +
            "                 union\n" +
            "                 SELECT distinct build_props.prop_key,build_props.prop_value,'Removed' status,'removed' removed from  build_props inner join\n" +
            "                   builds on builds.build_id = build_props.build_id where build_props.prop_key not in\n" +
            "                                                                          (SELECT distinct build_props.prop_key from  build_props inner join\n" +
            "                                                                            builds on builds.build_id = build_props.build_id where builds.build_number = ? and builds.build_date = ?)  and\n" +
            "                                                                          builds.build_number = ? and builds.build_date = ?\n" +
            "                 union\n" +
            "                 select r.c,r.v,r.st1,r.vb from (\n" +
            "                                             select * from (\n" +
            "                                                             SELECT distinct  build_props.prop_key c,build_props.prop_value  v,'Updated' st1 from  build_props inner join\n" +
            "                                                               builds on builds.build_id = build_props.build_id where\n" +
            "                                                               builds.build_number = ? and builds.build_date = ?) k\n" +
            "                                               left  join\n" +
            "                                               (SELECT distinct  build_props.prop_key b,build_props.prop_value vb,'Updated' status from  build_props inner join\n" +
            "                                                 builds on builds.build_id = build_props.build_id where\n" +
            "                                                 builds.build_number =?   and builds.build_date =? )t  on t.b = k.c where t.vb != k.v) r\n" +
            "                 union\n" +
            "                 select r.c,r.v,r.st1,r.v from (\n" +
            "                                             select * from (\n" +
            "                                                             SELECT distinct  build_props.prop_key c,build_props.prop_value v,'Unchanged' st1 from  build_props inner join\n" +
            "                                                               builds on builds.build_id = build_props.build_id where\n" +
            "                                                               builds.build_number = ? and builds.build_date = ?) k\n" +
            "                                               left  join\n" +
            "                                               (SELECT distinct  build_props.prop_key b,build_props.prop_value vb,'Unchanged' status from  build_props inner join\n" +
            "                                                 builds on builds.build_id = build_props.build_id where\n" +
            "                                                 builds.build_number =? and builds.build_date = ?)t  on t.b = k.c where t.vb = k.v)  r) z order by 1";

        public final static String BUILD_PROPS_COUNT = "\tselect count(*) from (\n" +
                "    \n" +
                "    SELECT distinct build_props.prop_key,build_props.prop_value from  build_props \n" +
                "    inner join \n" +
                "\tbuilds on builds.build_id = build_props.build_id where builds.build_number = ? and builds.build_date = ?\n" +
                "union \n" +
                "\tSELECT distinct build_props.prop_key,build_props.prop_value from  build_props inner join \n" +
                "\tbuilds on builds.build_id = build_props.build_id where build_props.prop_key not in \n" +
                "\t(SELECT distinct build_props.prop_key from  build_props inner join \n" +
                "\tbuilds on builds.build_id = build_props.build_id where builds.build_number = ? and  builds.build_date = ?)  and \n" +
                "\tbuilds.build_number = ? and  builds.build_date = ? ) z ";

    public final static String BUILD_ENV_PROPS = "\n" +
            "SELECT distinct build_props.prop_key propsKey,build_props.prop_value propsValue from  build_props\n" +
            "  inner join\n" +
            "  builds on builds.build_id = build_props.build_id where builds.build_number = ?\n" +
            "                                                         and builds.build_date = ?\n" +
            "                                                         and build_props.prop_key like 'buildInfo.env.%'";

    public final static String BUILD_SYSTEM_PROPS = "\n" +
            "SELECT distinct build_props.prop_key propsKey,build_props.prop_value propsValue from  build_props\n" +
            "  inner join\n" +
            "  builds on builds.build_id = build_props.build_id where builds.build_number = ?\n" +
            "                                                         and builds.build_date = ?\n" +
            "                                                         and build_props.prop_key not in (  SELECT distinct build_props.prop_key from  build_props inner join\n" +
            "                                                                                            builds on builds.build_id = build_props.build_id where builds.build_number = ?\n" +
            "                                                                                            and builds.build_date = ?\n" +
            "                                                                                            and build_props.prop_key like 'buildInfo.env.%' )\n";

    public final static String BUILD_ENV_PROPS_COUNT = "\n" +
            "SELECT count(*) from  build_props\n" +
            "  inner join\n" +
            "  builds on builds.build_id = build_props.build_id where builds.build_number = ?\n" +
            "                                                         and builds.build_date = ?\n" +
            "                                                         and build_props.prop_key like 'buildInfo.env.%'";

    public final static String BUILD_SYSTEM_PROPS_COUNT = "\n" +
            "SELECT count(*) from  build_props\n" +
            "  inner join\n" +
            "  builds on builds.build_id = build_props.build_id where builds.build_number = ?\n" +
            "                                                         and builds.build_date = ?\n" +
            "                                                         and build_props.prop_key not in (  SELECT distinct build_props.prop_key from  build_props inner join\n" +
            "                                                                                            builds on builds.build_id = build_props.build_id where builds.build_number = ?\n" +
            "                                                                                            and builds.build_date = ?\n" +
            "                                                                                            and build_props.prop_key like 'buildInfo.env.%' )\n";
}
