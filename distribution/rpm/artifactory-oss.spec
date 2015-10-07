Name:           jfrog-artifactory-oss
Provides:       artifactory = %{artifactory_version}
Obsoletes:      artifactory >= 3.6.0
Conflicts:      jfrog-artifactory-pro jfrog-artifactory-vrcs artifactory < 3.6.0
Version:        %{artifactory_version}
Release:        %{artifactory_release}
Summary:        Binary Repository Manager
Vendor:         JFrog Ltd.
Group:          Development/Tools
License:        LGPL
URL:            http://www.jfrog.org
Source0:        standalone.zip
BuildRoot:      %{_tmppath}/build-%{name}-%{version}
BuildArch:      noarch
Requires:       %{_sbindir}/useradd, %{_sbindir}/groupadd, %{_bindir}/pkill, %{_bindir}/rsync

%define username artifactory
%define group_name artifactory
%define extracted_standalone %{_sourcedir}/*artifactory*
%define extracted_tomcat %{extracted_standalone}/tomcat

#% define _rpmfilename %%{NAME}-%%{VERSION}-%%{RELEASE}.%%{ARCH}.rpm
%define _rpmfilename %{filename_prefix}-%{full_version}.rpm

%define target_jfrog_home /opt/jfrog
%define target_artifactory_install %{target_jfrog_home}/artifactory
%define target_jfrog_doc %{target_jfrog_home}/doc/artifactory-%{full_version}
%define	target_tomcat_home %{target_artifactory_install}/tomcat
%define target_etc_dir /etc%{target_artifactory_install}
%define target_var_dir /var%{target_jfrog_home}
%define target_artifactory_home %{target_var_dir}/artifactory

%description
The best binary repository manager around.

%prep
%setup -q -T -c
%{__unzip} "%{SOURCE0}" -d "%{_sourcedir}"

%build

%install
%__rm -rf %{buildroot}

# Install and copy the files in the install dir (opt) directories
%__install -d "%{buildroot}%{target_jfrog_home}"
%__install -d "%{buildroot}%{target_artifactory_install}/bin"
%__install -d "%{buildroot}%{target_artifactory_install}/misc"
%__install -d "%{buildroot}%{target_tomcat_home}/webapps"

# The war file is in Artifactory not tomcat webapps
%__install -D %{extracted_standalone}/webapps/artifactory.war "%{buildroot}%{target_artifactory_install}/webapps/artifactory.war"

%__cp -r %{extracted_standalone}/bin/* "%{buildroot}%{target_artifactory_install}/bin/"
%__cp -r %{extracted_standalone}/misc/* "%{buildroot}%{target_artifactory_install}/misc/"
rsync -r --exclude 'work' --exclude 'temp'  %{extracted_tomcat}/* "%{buildroot}%{target_tomcat_home}/"
%__cp %{extracted_standalone}/misc/service/setenv.sh "%{buildroot}%{target_tomcat_home}/bin/"

# Copy the etc dir to the from the build dir to the build root (currently contains default script)
%__install -d "%{buildroot}%{target_etc_dir}"

# Copy the contents of the standalone etc to the artifactory etc dir
%__cp -r %{extracted_standalone}/etc/* "%{buildroot}%{target_etc_dir}"
%__install -D %{extracted_standalone}/bin/artifactory.default "%{buildroot}%{target_etc_dir}/default"
%__install -D %{extracted_standalone}/misc/service/artifactory "%{buildroot}%{_sysconfdir}/init.d/artifactory"

# Replace the vars in the init and default scripts
%__sed -r --in-place "s%#export ARTIFACTORY_HOME=.*%export ARTIFACTORY_HOME=%{target_artifactory_home}%g;" "%{buildroot}%{target_etc_dir}/default"
%__sed -r --in-place "s%export TOMCAT_HOME=.*%export TOMCAT_HOME=%{target_tomcat_home}%g;" "%{buildroot}%{target_etc_dir}/default"
%__sed -r --in-place "s/#export ARTIFACTORY_USER=.*/export ARTIFACTORY_USER=%{username}/g;" "%{buildroot}%{target_etc_dir}/default"
%__sed -r --in-place "s%export ARTIFACTORY_PID=.*%export ARTIFACTORY_PID=%{target_var_dir}/run/artifactory.pid%g;" "%{buildroot}%{target_etc_dir}/default"

# Create artifactory home dir (var) and symlinks to install dir (opt)
%__install -d "%{buildroot}%{target_artifactory_home}"
%__install -d "%{buildroot}%{target_var_dir}/run"

# Link the folders
%__ln_s "%{target_artifactory_install}/webapps" "%{buildroot}%{target_artifactory_home}/webapps"
%__ln_s "%{target_artifactory_home}/temp" "%{buildroot}%{target_tomcat_home}/temp"
%__ln_s "%{target_artifactory_home}/work" "%{buildroot}%{target_tomcat_home}/work"
%__ln_s "/etc/opt/jfrog/artifactory" "%{buildroot}/var/opt/jfrog/artifactory/etc"
%__ln_s "%{target_artifactory_install}/misc" "%{buildroot}%{target_artifactory_home}/misc"
%__ln_s "%{target_artifactory_install}/tomcat" "%{buildroot}%{target_artifactory_home}/tomcat"

# log directories installation
%__ln_s "%{target_artifactory_home}/logs/catalina" "%{buildroot}%{target_tomcat_home}/logs"

# Fill the documentation
%__install -d "%{buildroot}%{target_jfrog_doc}"
%__cp %{extracted_standalone}/Third-Parties-Usage-About-Box.html "%{buildroot}%{target_jfrog_doc}"
%__cp %{extracted_standalone}/COPYING "%{buildroot}%{target_jfrog_doc}"
%__cp %{extracted_standalone}/COPYING.LESSER "%{buildroot}%{target_jfrog_doc}"
%__cp %{extracted_standalone}/README.txt "%{buildroot}%{target_jfrog_doc}"

%clean
%__rm -rf %{_builddir}/artifactory-%{version}

%pre

CURRENT_USER=`id -nu`
if [ "$CURRENT_USER" != "root" ]; then
    echo
    echo "ERROR: Please install Artifactory using root."
    echo
    exit 1
fi

SERVICE_FILE="%{_sysconfdir}/init.d/artifactory"
if [ -e "$SERVICE_FILE" ]; then
    # Checking same layout. If not fails upgrade
    if [ -z "`grep "%{target_etc_dir}/default" "$SERVICE_FILE"`" ]; then
        echo "ERROR: Currently installed Artifactory version does not have the same layout than this RPM!"
        echo "NOTE: To upgrade follow these instructions:"
        echo "NOTE: - Uninstall the previous RPM (rpm -e artifactory),"
        echo "NOTE: - Then install this one (rpm -i this_rpm),"
        echo "NOTE: - And finally recover from backup (/opt/jfrog/artifactory/bin/recover.backup.sh)"
        exit 1
    fi

    # Shutting down the artifactory service if running
    SERVICE_STATUS="`$SERVICE_FILE status`"
    if [[ ! "$SERVICE_STATUS" =~ .*[sS]topped.* ]]; then
        echo "Stopping the artifactory service..."
        $SERVICE_FILE stop || exit $?
    fi
fi

[ -e /etc/opt/jfrog/artifactory ] && read uid gid <<<$(stat -c '%u %g' /etc/opt/jfrog/artifactory)

echo "Checking if group %{group_name} exists..."
getent group "%{group_name}" 2>&1 1>/dev/null
if [ $? != 0  ]; then
  echo "Group %{group_name} doesn't exist. Creating ..."
  %{_sbindir}/groupadd -r %{group_name} ${gid:+-g} $gid|| exit $?
else
  echo "Group %{group_name} exists."
fi

echo "Checking if ARTIFACTORY_HOME exists"
if [ ! -d "%{target_artifactory_home}" ]; then
  %__mkdir_p %{target_artifactory_home}
fi

echo "Checking if user %{username} exists..."

getent passwd "%{username}" 2>&1 1>/dev/null
if [ $? != 0 ]; then
  echo "User %{username} doesn't exist. Creating ..."
  %{_sbindir}/useradd %{username} -g %{username} -d %{target_artifactory_home} ${uid:+-u} $uid || exit $?
  chown %{username} %{target_artifactory_home} || exit $?
else
  echo "User %{username} exists."
fi

# Cleaning the artifactory webapp and work folder

echo "Removing tomcat work directory"
if [ -d %{target_tomcat_home}/work ]; then
  %__rm -rf %{target_tomcat_home}/work || exit $?
fi

if [ -d %{target_tomcat_home}/webapps/artifactory ]; then
  echo "Removing Artifactory's exploded WAR directory"
  %__rm -rf %{target_tomcat_home}/webapps/artifactory || exit $?
fi
exit 0

%post

if [ "$1" = "1" ]; then
  echo "Adding the artifactory service to auto-start"
  /sbin/chkconfig --add artifactory || $?

  echo
  echo "The installation of Artifactory has completed successfully."
  echo
  echo "PLEASE NOTE: You can recover a backup done with Artifactory RPM 3.0 and above using '/opt/jfrog/artifactory/bin/recover.backup.sh'. For upgrading from previous version of Artifactory please refer to the wiki http://wiki.jfrog.org/confluence/display/RTF/Upgrading+Artifactory"
  echo "PLEASE NOTE: It is highly recommended to use Artifactory in conjunction with MySQL. You can easily configure this setup using '/opt/jfrog/artifactory/bin/configure.mysql.sh'."
  echo
elif [ "$1" = "2" ]; then
  echo "The upgrade of Artifactory has completed successfully."
fi

%preun
if [ "$1" = "0" ]; then
  # It's an un-installation

  CURRENT_USER=`id -nu`
  if [ "$CURRENT_USER" != "root" ]; then
    echo
    echo "ERROR: Please un-install Artifactory using root."
    echo
    exit 1
  fi

  SERVICE_FILE=%{_sysconfdir}/init.d/artifactory

  if [ -f $SERVICE_FILE ]; then
    SERVICE_STATUS="`$SERVICE_FILE status`"
    if [[ ! "$SERVICE_STATUS" =~ .*[sS]topped.* ]]; then
      echo "Stopping the artifactory service..."
      $SERVICE_FILE stop || exit $?
    fi
  fi

  echo "Removing the artifactory service from auto-start"
  /sbin/chkconfig --del artifactory 2>/dev/null

  # Create backups
  echo "Creating a backup of the artifactory home folder in ${BACKUP_DIR}"
  TIMESTAMP=`echo "$(date '+%T')" | tr -d ":"`
  CURRENT_TIME="$(date '+%Y%m%d').$TIMESTAMP"
  BACKUP_DIR="%{target_var_dir}/artifactory.backup.${CURRENT_TIME}"

  # ignore any failures or the package will be left in an inconsistent state
  %__mkdir_p "${BACKUP_DIR}" && \
  %__cp -a %{target_etc_dir} "${BACKUP_DIR}/etc" && \
  %__mv %{target_artifactory_home}/logs "${BACKUP_DIR}/logs" 2>&1 1>/dev/null

  %__rm -rf "%{target_artifactory_home}/data/tmp" 2>&1 1>/dev/null
  %__rm -rf "%{target_artifactory_home}/data/work" 2>&1 1>/dev/null

  if [ -e %{target_tomcat_home}/lib/mysql-connector-java*.jar ]; then
    echo "MySQL connector found"
    %__cp %{target_tomcat_home}/lib/mysql-connector-java* "${BACKUP_DIR}" 2>/dev/null
  fi
  if [ -e %{target_artifactory_home}/backup ]; then
    %__mv %{target_artifactory_home}/backup "${BACKUP_DIR}/backup" 2>/dev/null
  fi
fi

exit 0

%postun
if [ "$1" = "0" ]; then
  # It's an un-installation

  echo "Logging off user %{username}"
  %{_bindir}/pkill -KILL -u %{username}

  %__rm -rf %{target_artifactory_home}/{work,temp} 2>/dev/null

  # Ignoring user folders since the home dir is deleted already by the RPM spec
  echo "Removing local user %{username}"
  ( grep -q %{username} /etc/passwd &&
  %{_sbindir}/userdel %{username} 2>/dev/null) || echo $?

  EXISTING_GROUP="`grep %{group_name} /etc/group | awk -F ':' '{ print $1 }' 2>/dev/null`"
  if [ "$EXISTING_GROUP" == "%{group_name}" ]; then
    echo "Removing group %{group_name}"
    %{_sbindir}/groupdel %{group_name}
  fi

fi
exit 0

%posttrans
echo post transaction %{name} \$1 = $1 >>/tmp/rpminst
[ -e /etc/opt/jfrog/artifactory ] && read uid gid <<<$(stat -c '%u %g' /etc/opt/jfrog/artifactory)

echo "Checking if group %{group_name} exists..."
getent group "%{group_name}" 2>&1 1>/dev/null
if [ $? != 0  ]; then
  echo "Group %{group_name} doesn't exist. Creating ..."
  %{_sbindir}/groupadd -r %{group_name} ${gid:+-g} $gid|| exit $?
else
  echo "Group %{group_name} exists."
fi

echo "Checking if ARTIFACTORY_HOME exists"
if [ ! -d "%{target_artifactory_home}" ]; then
  %__mkdir_p %{target_artifactory_home}
fi

echo "Checking if user %{username} exists..."

getent passwd "%{username}" 2>&1 1>/dev/null
if [ $? != 0 ]; then
  echo "User %{username} doesn't exist. Creating ..."
  %{_sbindir}/useradd %{username} -g %{username} -d %{target_artifactory_home} ${uid:+-u} $uid || exit $?
  chown %{username} %{target_artifactory_home} || exit $?
else
  echo "User %{username} exists."
fi

/sbin/chkconfig --list artifactory 2>&1 1>/dev/null || \
( /sbin/chkconfig --add artifactory && \
echo "Adding the artifactory service to auto-start" )

exit 0

%triggerpostun -- artifactory
echo trigger post uninstall %{name} \$1 = $1 >>/tmp/rpminst
exit 0

%triggerun -- artifactory
echo trigger uninstall %{name} \$1 = $1 >>/tmp/rpminst
exit 0

%files
%dir %{target_jfrog_home}
%dir %{target_jfrog_doc}
%dir %{target_artifactory_install}
%dir %{target_jfrog_home}/doc
%attr(775,root,root) %config %{_sysconfdir}/init.d/artifactory
%attr(775,root,root) %dir %{target_jfrog_home}
%attr(775,root,root) %config %{target_artifactory_install}/bin
%config %{target_artifactory_install}/misc
%config %{target_artifactory_install}/webapps
%attr(774,root,root) %{target_jfrog_doc}/Third-Parties-Usage-About-Box.html
%attr(774,root,root) %{target_jfrog_doc}/COPYING
%attr(774,root,root) %{target_jfrog_doc}/COPYING.LESSER
%attr(774,root,root) %{target_jfrog_doc}/README.txt
%attr(775,artifactory,artifactory) %dir %{target_tomcat_home}
%attr(775,root,root) %config %{target_tomcat_home}/bin
%attr(775,root,root) %config %{target_tomcat_home}/conf
%attr(775,root,root) %{target_tomcat_home}/lib
%{target_tomcat_home}/logs
%{target_tomcat_home}/temp
%{target_tomcat_home}/work
%attr(775,artifactory,artifactory) %{target_artifactory_home}
%attr(775,artifactory,artifactory) %{target_tomcat_home}/webapps
%attr(774,root,root) %{target_tomcat_home}/LICENSE
%attr(774,root,root) %{target_tomcat_home}/NOTICE
%attr(774,root,root) %{target_tomcat_home}/RELEASE-NOTES
%attr(774,root,root) %{target_tomcat_home}/RUNNING.txt

%defattr(770,%{username}, %{group_name}, -)
%{target_var_dir}/run
%dir %{target_etc_dir}
%dir /etc/opt/jfrog
%config(noreplace) %{target_etc_dir}/artifactory.system.properties
%config(missingok) %{target_etc_dir}/artifactory.config.xml
%config(noreplace) %{target_etc_dir}/default
%config(noreplace) %{target_etc_dir}/logback.xml
%config %{target_etc_dir}/mimetypes.xml

%doc
