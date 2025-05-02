# Service Builder

## Liferay with MySQL

1. Start MySQL with the following command.

	```
	d run \
		--name ephesians-mysql \
		--rm \
		-e MYSQL_ALLOW_EMPTY_PASSWORD=yes \
		-e MYSQL_DATABASE=lportal \
		-e MYSQL_PASSWORD=test \
		-e MYSQL_USER=test \
		-it \
		-p 3306:3306 \
		mysql:8 \
		--character-set-server=utf8mb4 \
		--collation-server=utf8mb4_unicode_ci
	```

	Notice the name given to the MySQL container.

1. Type ***d exec -it ephesians-mysql mysql -utest -ptest*** to connect to MySQL.

1. Type ***use lportal;*** to go into the ***lportal*** database.

1. Type ***show tables;*** to see that there are no tables.

1. Start Liferay with the following command.

	```
	docker run \
		--link ephesians-mysql \
		--name ephesians-liferay \
		--rm \
		-e LIFERAY_JDBC_PERIOD_DEFAULT_PERIOD_DRIVER_UPPERCASEC_LASS_UPPERCASEN_AME="com.mysql.cj.jdbc.Driver" \
		-e LIFERAY_JDBC_PERIOD_DEFAULT_PERIOD_PASSWORD="test" \
		-e LIFERAY_JDBC_PERIOD_DEFAULT_PERIOD_URL="jdbc:mysql://ephesians-mysql/lportal?characterEncoding=UTF-8&useFastDateParsing=false&useUnicode=true" \
		-e LIFERAY_JDBC_PERIOD_DEFAULT_PERIOD_USERNAME="test" \
		-it \
		-p 8080:8080 \
		liferay/portal:7.4.3.120-ga120
	```

	Notice the name given to the Liferay container and how it is linked to the MySQL container.

1. Verify on the Liferay console that it is connected to MySQL.

	```
	2021-03-24 01:03:33.351 INFO  [main][DialectDetector:159] Using dialect org.hibernate.dialect.MySQLDialect for MySQL 8.0
	2021-03-24 01:03:33.409 INFO  [main][DBInitUtil:132] Create tables and populate with default data
	```

1. Type ***show tables;*** to see that many tables were created.

1. Notice that it took longer for Liferay to startup.

	```
	24-Mar-2021 01:04:30.979 INFO [main] org.apache.catalina.startup.Catalina.start Server startup in [64616] milliseconds
	```

	Why? The default Liferay Docker image runs with an embedded Hypersonic database that includes prepopulated data. Since this is connecting to a vanilla MySQL database with no tables, Liferay had to create the tables and populate the data.

	Below is a small sample of those log messages.

	```
	2021-03-24 01:04:04.272 INFO  [main][LoggingTimer:83] Starting com.liferay.portal.events.StartupHelperUtil#initResourceActions
	2021-03-24 01:04:04.466 INFO  [main][LoggingTimer:44] Completed com.liferay.portal.events.StartupHelperUtil#initResourceActions in 194 ms
	2021-03-24 01:04:04.472 INFO  [main][VerifyProcess:65] Verifying com.liferay.portal.verify.VerifyProperties
	2021-03-24 01:04:04.472 INFO  [main][LoggingTimer:83] Starting com.liferay.portal.verify.VerifyProperties#verifySystemProperties
	2021-03-24 01:04:04.472 INFO  [main][LoggingTimer:44] Completed com.liferay.portal.verify.VerifyProperties#verifySystemProperties in 0 ms
	2021-03-24 01:04:04.473 INFO  [main][LoggingTimer:83] Starting com.liferay.portal.verify.VerifyProperties#verifyPortalProperties
	2021-03-24 01:04:04.480 INFO  [main][LoggingTimer:44] Completed com.liferay.portal.verify.VerifyProperties#verifyPortalProperties in 7 ms
	2021-03-24 01:04:04.481 INFO  [main][LoggingTimer:83] Starting com.liferay.portal.verify.VerifyProperties#verifyDocumentLibrary
	```

1. Type ***<Control+C>*** to stop Liferay. Type ***\<Up\>*** and ***\<Enter\>*** to restart Liferay. Notice that Liferay started faster this time and that there are fewer log messages. Why?

## service.xml

1. Service Builder is a code generator. Its main input file is ***service.xml*** and it outputs Java code that makes it easier for the developer to interact with the database without having to write native SQL.

1. Create and go to a directory called ***my-service-builder-project***. Follow the steps in [Set Up an OSGi Project](https://github.com/liferay/liferay-basic-training/tree/master/docs/osgi#set-up-an-osgi-project).

   Don't use ***echo "liferay.workspace.product=portal-7.4-ga90" > gradle.properties*** use ***echo "liferay.workspace.product-dxp2025.q1.2-lts" > gradle.properties*** in the osgi step.

1. Type ***mkdir -p h7g5-api***. The prefix ***h7g5*** is just a random name used for this tutorial.

1. Type ***osub h7g5-api/bnd.bnd*** and paste the following code.

	```
	Bundle-Name: Liferay H7G5 API
	Bundle-SymbolicName: com.liferay.h7g5.api
	Bundle-Version: 1.0.0
	```

1. Type ***osub h7g5-api/build.gradle*** and paste the following code.

	```
	dependencies {
		compileOnly group: "com.liferay.portal", name: "release.dxp.api", version: "2025.q1.2"
	}
	```

1. Type ***./gradlew h7g5-api:classes && la h7g5-api/build*** to verify that h7g5-api was created properly.

1. Type ***mkdir -p h7g5-service/src***.

1. Type ***osub h7g5-service/bnd.bnd*** and paste the following code.

	```
	Bundle-Name: Liferay H7G5 Service
	Bundle-SymbolicName: com.liferay.h7g5.service
	Bundle-Version: 1.0.0
	```

1. Type ***osub h7g5-service/build.gradle*** and paste the following code.

	```
	apply plugin: "com.liferay.portal.tools.service.builder"
	
	tasks.named(buildService) {
		apiDir = "../h7g5-api/src/main/java"
		testDir = "../h7g5-test/src/testIntegration/java"
	}

	dependencies {
		compileOnly group: "com.liferay.portal", name: "release.dxp.api", version: "2025.q1.2"
		compileOnly project(":h7g5-api")
	}

1. Type ***./gradlew h7g5-service:classes && la h7g5-service/build*** to verify that h7g5-service was created properly.

1. Type ***osub h7g5-service/service.xml*** and paste the following code.

	```
	<?xml version="1.0"?>
	<!DOCTYPE service-builder PUBLIC "-//Liferay//DTD Service Builder 7.4.0//EN" "http://www.liferay.com/dtd/liferay-service-builder_7_4_0.dtd">

	<service-builder auto-namespace-tables="true" dependency-injector="ds" package-path="com.liferay.h7g5">
		<namespace>OHQIWTSFHL</namespace>
		<entity local-service="true" name="H7G5Entry" remote-service="true">

			<!-- PK fields -->

			<column name="h7g5EntryId" primary="true" type="long" />

			<!-- Group instance -->

			<column name="groupId" type="long" />

			<!-- Audit fields -->

			<column name="companyId" type="long" />
			<column name="userId" type="long" />
			<column name="userName" type="String" />
			<column name="createDate" type="Date" />
			<column name="modifiedDate" type="Date" />

			<!-- Other fields -->

			<column name="h7g5FolderId" type="long" />
			<column name="description" type="String" />
			<column name="key" type="String" />
			<column name="name" type="String" />

			<!-- Order -->

			<order by="desc">
				<order-column name="createDate" />
			</order>

			<!-- Finder methods -->

			<finder name="H7G5FolderId" return-type="Collection">
				<finder-column name="h7g5FolderId" />
			</finder>
			<finder name="Key" return-type="H7G5Entry">
				<finder-column name="key" />
			</finder>
			<finder name="Name" return-type="Collection">
				<finder-column name="name" />
			</finder>
			<finder name="H_D_N" return-type="H7G5Entry">
				<finder-column name="h7g5FolderId" />
				<finder-column name="description" />
				<finder-column name="name" />
			</finder>

			<!-- References -->

			<reference entity="Group" package-path="com.liferay.portal" />
		</entity>
		<entity local-service="true" name="H7G5Folder" remote-service="true">

			<!-- PK fields -->

			<column name="h7g5FolderId" primary="true" type="long" />

			<!-- Group instance -->

			<column name="groupId" type="long" />

			<!-- Audit fields -->

			<column name="companyId" type="long" />
			<column name="userId" type="long" />
			<column name="userName" type="String" />
			<column name="createDate" type="Date" />
			<column name="modifiedDate" type="Date" />

			<!-- Other fields -->

			<column name="description" type="String" />
			<column name="name" type="String" />

			<!-- Order -->

			<order by="desc">
				<order-column name="createDate" />
			</order>
		</entity>
		<exceptions>
			<exception>DuplicateH7G5EntryKey</exception>
			<exception>H7G5FolderName</exception>
		</exceptions>
	</service-builder>
	```

1. Type ***./gradlew h7g5-service:buildService***. The ***buildService*** task takes service.xml as the input file and generates many files.

	```
	Building H7G5Entry
	Writing src/main/java/com/liferay/h7g5/service/persistence/impl/H7G5EntryPersistenceImpl.java
	Writing ../h7g5-api/src/main/java/com/liferay/h7g5/service/persistence/H7G5EntryPersistence.java
	Writing ../h7g5-api/src/main/java/com/liferay/h7g5/service/persistence/H7G5EntryUtil.java
	Writing src/main/java/com/liferay/h7g5/model/impl/H7G5EntryModelImpl.java
	Writing src/main/java/com/liferay/h7g5/model/impl/H7G5EntryBaseImpl.java
	Writing src/main/java/com/liferay/h7g5/model/impl/H7G5EntryImpl.java
	Writing ../h7g5-api/src/main/java/com/liferay/h7g5/model/H7G5EntryModel.java
	Writing ../h7g5-api/src/main/java/com/liferay/h7g5/model/H7G5Entry.java
	Writing src/main/java/com/liferay/h7g5/model/impl/H7G5EntryCacheModel.java
	Writing ../h7g5-api/src/main/java/com/liferay/h7g5/model/H7G5EntryWrapper.java
	Writing ../h7g5-api/src/main/java/com/liferay/h7g5/model/H7G5EntrySoap.java
	Writing src/main/java/com/liferay/h7g5/service/impl/H7G5EntryLocalServiceImpl.java
	Writing src/main/java/com/liferay/h7g5/service/base/H7G5EntryLocalServiceBaseImpl.java
	Writing ../h7g5-api/src/main/java/com/liferay/h7g5/service/H7G5EntryLocalService.java
	Writing ../h7g5-api/src/main/java/com/liferay/h7g5/service/H7G5EntryLocalServiceUtil.java
	Writing ../h7g5-api/src/main/java/com/liferay/h7g5/service/H7G5EntryLocalServiceWrapper.java
	Writing src/main/java/com/liferay/h7g5/service/impl/H7G5EntryServiceImpl.java
	Writing src/main/java/com/liferay/h7g5/service/base/H7G5EntryServiceBaseImpl.java
	Writing ../h7g5-api/src/main/java/com/liferay/h7g5/service/H7G5EntryService.java
	Writing ../h7g5-api/src/main/java/com/liferay/h7g5/service/H7G5EntryServiceUtil.java
	Writing ../h7g5-api/src/main/java/com/liferay/h7g5/service/H7G5EntryServiceWrapper.java
	Writing src/main/java/com/liferay/h7g5/service/http/H7G5EntryServiceHttp.java
	Writing src/main/java/com/liferay/h7g5/service/http/H7G5EntryServiceSoap.java
	Building H7G5Folder
	Writing src/main/java/com/liferay/h7g5/service/persistence/impl/H7G5FolderPersistenceImpl.java
	Writing ../h7g5-api/src/main/java/com/liferay/h7g5/service/persistence/H7G5FolderPersistence.java
	Writing ../h7g5-api/src/main/java/com/liferay/h7g5/service/persistence/H7G5FolderUtil.java
	Writing src/main/java/com/liferay/h7g5/model/impl/H7G5FolderModelImpl.java
	Writing src/main/java/com/liferay/h7g5/model/impl/H7G5FolderBaseImpl.java
	Writing src/main/java/com/liferay/h7g5/model/impl/H7G5FolderImpl.java
	Writing ../h7g5-api/src/main/java/com/liferay/h7g5/model/H7G5FolderModel.java
	Writing ../h7g5-api/src/main/java/com/liferay/h7g5/model/H7G5Folder.java
	Writing src/main/java/com/liferay/h7g5/model/impl/H7G5FolderCacheModel.java
	Writing ../h7g5-api/src/main/java/com/liferay/h7g5/model/H7G5FolderWrapper.java
	Writing ../h7g5-api/src/main/java/com/liferay/h7g5/model/H7G5FolderSoap.java
	Writing src/main/java/com/liferay/h7g5/service/impl/H7G5FolderLocalServiceImpl.java
	Writing src/main/java/com/liferay/h7g5/service/base/H7G5FolderLocalServiceBaseImpl.java
	Writing ../h7g5-api/src/main/java/com/liferay/h7g5/service/H7G5FolderLocalService.java
	Writing ../h7g5-api/src/main/java/com/liferay/h7g5/service/H7G5FolderLocalServiceUtil.java
	Writing ../h7g5-api/src/main/java/com/liferay/h7g5/service/H7G5FolderLocalServiceWrapper.java
	Writing src/main/java/com/liferay/h7g5/service/impl/H7G5FolderServiceImpl.java
	Writing src/main/java/com/liferay/h7g5/service/base/H7G5FolderServiceBaseImpl.java
	Writing ../h7g5-api/src/main/java/com/liferay/h7g5/service/H7G5FolderService.java
	Writing ../h7g5-api/src/main/java/com/liferay/h7g5/service/H7G5FolderServiceUtil.java
	Writing ../h7g5-api/src/main/java/com/liferay/h7g5/service/H7G5FolderServiceWrapper.java
	Writing src/main/java/com/liferay/h7g5/service/http/H7G5FolderServiceHttp.java
	Writing src/main/java/com/liferay/h7g5/service/http/H7G5FolderServiceSoap.java
	Writing src/main/resources/META-INF/module-hbm.xml
	Writing src/main/resources/META-INF/portlet-model-hints.xml
	Writing ../h7g5-api/src/main/java/com/liferay/h7g5/exception/DuplicateH7G5EntryKeyException.java
	Writing ../h7g5-api/src/main/java/com/liferay/h7g5/exception/H7G5FolderNameException.java
	Writing ../h7g5-api/src/main/java/com/liferay/h7g5/exception/NoSuchH7G5EntryException.java
	Writing ../h7g5-api/src/main/java/com/liferay/h7g5/exception/NoSuchH7G5FolderException.java
	Writing src/main/java/com/liferay/h7g5/service/persistence/impl/constants/OHQIWTSFHLPersistenceConstants.java
	Writing src/main/resources/META-INF/sql/indexes.sql
	Writing src/main/resources/META-INF/sql/tables.sql
	Writing src/main/resources/META-INF/sql/tables.sql
	Writing src/main/resources/META-INF/sql/tables.sql
	Writing src/main/resources/service.properties
	```	

	The file paths are relative to h7g5-service. Open up these files with Sublime. Some files are obvious in what they do (e.g. tables.sql). Some files are less obvious (e.g. H7G5FolderSoap.java). That's OK. Just look at them so they are logged in your mind.

1. Type ***./gradlew classes*** to make sure the generated files compile.

1. In MySQL, type ***show tables;***.

	1. Type ***desc User;***.

	1. Type ***desc SomeInvalidTableName;***

	1. Type ***desc OHQIWTSFHL_H7G5Entry;***. Where did I get this name from? The table does not yet exist.

1. Type ***./gradlew deploy -Ddeploy.docker.container.id=ephesians-liferay*** to deploy h7g5-api and h7g5-service.

1. Verify on the Liferay console that ***com.liferay.h7g5.service*** could not resolve.

	```
	2021-03-25 11:19:08.675 INFO  [fileinstall-directory-watcher][BundleStartStopLogger:46] STARTED com.liferay.h7g5.api_1.0.0 [1355]
	2021-03-25 11:19:08.682 ERROR [fileinstall-directory-watcher][DirectoryWatcher:1136] Unable to start bundle: file:/opt/liferay/osgi/modules/com.liferay.h7g5.service-1.0.0.jar
	org.osgi.framework.BundleException: Could not resolve module: com.liferay.h7g5.service [1356]_  Unresolved requirement: Import-Package: com.liferay.h7g5.exception_ [Sanitized]
		at org.eclipse.osgi.container.Module.start(Module.java:444)
		at org.eclipse.osgi.internal.framework.EquinoxBundle.start(EquinoxBundle.java:428)
		at com.liferay.portal.file.install.internal.DirectoryWatcher._startBundle(DirectoryWatcher.java:1119)
		at com.liferay.portal.file.install.internal.DirectoryWatcher._startBundles(DirectoryWatcher.java:1152)
		at com.liferay.portal.file.install.internal.DirectoryWatcher._process(DirectoryWatcher.java:1014)
		at com.liferay.portal.file.install.internal.DirectoryWatcher.run(DirectoryWatcher.java:265)	
	```

	The module ***com.liferay.h7g5.service*** could not resolve because it requires the package ***com.liferay.h7g5.exception***. How can we fix this exception?

1. Type ***./gradlew deploy -Ddeploy.docker.container.id=ephesians-liferay***.

	```
	2021-03-25 11:22:59.144 ERROR [fileinstall-directory-watcher][DirectoryWatcher:1136] Unable to start bundle: file:/opt/liferay/osgi/modules/com.liferay.h7g5.service-1.0.0.jar
	org.osgi.framework.BundleException: Could not resolve module: com.liferay.h7g5.service [1356]_  Unresolved requirement: Import-Package: com.liferay.h7g5.model_ [Sanitized]
		at org.eclipse.osgi.container.Module.start(Module.java:444)
		at org.eclipse.osgi.internal.framework.EquinoxBundle.start(EquinoxBundle.java:428)
		at com.liferay.portal.file.install.internal.DirectoryWatcher._startBundle(DirectoryWatcher.java:1119)
		at com.liferay.portal.file.install.internal.DirectoryWatcher._startBundles(DirectoryWatcher.java:1152)
		at com.liferay.portal.file.install.internal.DirectoryWatcher._process(DirectoryWatcher.java:1014)
		at com.liferay.portal.file.install.internal.DirectoryWatcher.run(DirectoryWatcher.java:265)	
	```

	The module ***com.liferay.h7g5.service*** is not able to resolve because it is missing the package ***com.liferay.h7g5.model***.


1. Type ***./gradlew deploy -Ddeploy.docker.container.id=ephesians-liferay***.

	```
	2021-03-25 11:24:04.328 ERROR [fileinstall-directory-watcher][DirectoryWatcher:1136] Unable to start bundle: file:/opt/liferay/osgi/modules/com.liferay.h7g5.service-1.0.0.jar
	org.osgi.framework.BundleException: Could not resolve module: com.liferay.h7g5.service [1356]_  Unresolved requirement: Import-Package: com.liferay.h7g5.service_ [Sanitized]
		at org.eclipse.osgi.container.Module.start(Module.java:444)
		at org.eclipse.osgi.internal.framework.EquinoxBundle.start(EquinoxBundle.java:428)
		at com.liferay.portal.file.install.internal.DirectoryWatcher._startBundle(DirectoryWatcher.java:1119)
		at com.liferay.portal.file.install.internal.DirectoryWatcher._startBundles(DirectoryWatcher.java:1152)
		at com.liferay.portal.file.install.internal.DirectoryWatcher._process(DirectoryWatcher.java:1014)
		at com.liferay.portal.file.install.internal.DirectoryWatcher.run(DirectoryWatcher.java:265)	
	```

	The module ***com.liferay.h7g5.service*** is not able to resolve because it is missing the package ***com.liferay.h7g5.service***. This error can be confusing because the name of the missing package that is provided by the module ***com.liferay.h7g5.api*** happens to be the same name as the module ***com.liferay.h7g5.service***.

1. Type ***./gradlew deploy -Ddeploy.docker.container.id=ephesians-liferay***.

	```
	2021-03-25 11:27:19.767 ERROR [fileinstall-directory-watcher][DirectoryWatcher:1136] Unable to start bundle: file:/opt/liferay/osgi/modules/com.liferay.h7g5.service-1.0.0.jar
	org.osgi.framework.BundleException: Could not resolve module: com.liferay.h7g5.service [1356]_  Unresolved requirement: Import-Package: com.liferay.h7g5.service.persistence_ [Sanitized]
		at org.eclipse.osgi.container.Module.start(Module.java:444)
		at org.eclipse.osgi.internal.framework.EquinoxBundle.start(EquinoxBundle.java:428)
		at com.liferay.portal.file.install.internal.DirectoryWatcher._startBundle(DirectoryWatcher.java:1119)
		at com.liferay.portal.file.install.internal.DirectoryWatcher._startBundles(DirectoryWatcher.java:1152)
		at com.liferay.portal.file.install.internal.DirectoryWatcher._process(DirectoryWatcher.java:1014)
		at com.liferay.portal.file.install.internal.DirectoryWatcher.run(DirectoryWatcher.java:265)
	```

	The module ***com.liferay.h7g5.service*** is not able to resolve because it is missing the package ***com.liferay.h7g5.service.persistence***.

1. Verify on the Liferay console that both modules started.

	```
	2021-03-25 11:28:34.961 INFO  [Refresh Thread: Equinox Container: 743d7fd3-e7c1-438a-9dee-b8ecf6e55aa2][BundleStartStopLogger:46] STARTED com.liferay.h7g5.api_1.0.0 [1355]
	2021-03-25 11:28:34.962 INFO  [Refresh Thread: Equinox Container: 743d7fd3-e7c1-438a-9dee-b8ecf6e55aa2][BundleStartStopLogger:46] STARTED com.liferay.h7g5.service_1.0.0 [1356]
	```

1. To get the ***com.liferay.h7g5.service*** to resolve, you had to modify h7g5-api/bnd.bnd with the following new line.

 	```
 	Export-Package: com.liferay.h7g5.exception, com.liferay.h7g5.model, com.liferay.h7g5.service, com.liferay.h7g5.service.persistence
 	```

 	That can look ugly if you have to import many packages. Try formatting it like the following.

 	```
 	Bundle-Name: Liferay H7G5 API
	Bundle-SymbolicName: com.liferay.h7g5.api
	Bundle-Version: 1.0.0
	Export-Package:\
		com.liferay.h7g5.exception,\
		com.liferay.h7g5.model,\
		com.liferay.h7g5.service,\
		com.liferay.h7g5.service.persistence
	```

1. Go to MySQL and type ***desc OHQIWTSFHL_H7G5Entry;***. The table is still missing.

1. Add the following lines to ***h7g5-service/bnd.bnd***.

	```
	Liferay-Require-SchemaVersion: 1.0.0
	Liferay-Service: true
	```

	The ***Liferay-Service*** property tells Liferay to process ***com.liferay.h7g5.service*** as a Service Builder module. Be very careful not to accidentally add this line to ***h7g5-api/bnd.bnd***

1. Type ***./gradlew deploy -Ddeploy.docker.container.id=ephesians-liferay***.

1. Go to MySQL and type ***desc OHQIWTSFHL_H7G5Entry;***. The table was created.

	1. Type ***select * from OHQIWTSFHL_H7G5Entry;***.

	1. Type ***desc OHQIWTSFHL_H7G5Folder;***.

	1. Type ***select * from OHQIWTSFHL_H7G5Folder;***.

## Invoke Services from a Portlet

1. Type ***mkdir -p h7g5-web/src/main/java/com/liferay/h7g5/web/internal/portlet***.

1. Type ***osub h7g5-web/src/main/java/com/liferay/h7g5/web/internal/portlet/H7G5Portlet.java*** and paste the following code.

	```
	package com.liferay.h7g5.web.internal.portlet;

	import java.io.IOException;
	import java.io.PrintWriter;

	import javax.portlet.GenericPortlet;
	import javax.portlet.Portlet;
	import javax.portlet.RenderRequest;
	import javax.portlet.RenderResponse;

	import org.osgi.service.component.annotations.Component;

	@Component(
		property = {
			"com.liferay.portlet.display-category=category.sample",
			"javax.portlet.display-name=H7G5"
		},
		service = Portlet.class
	)
	public class H7G5Portlet extends GenericPortlet {

		public H7G5Portlet() {
			System.out.println("Constructing H7G5Portlet");
		}

		@Override
		protected void doView(
				RenderRequest renderRequest, RenderResponse renderResponse)
			throws IOException {

			System.out.println("Invoking H7G5Portlet#doView");

			PrintWriter printWriter = renderResponse.getWriter();

			printWriter.println("Hello, H7G5!");
		}

	}
	```

1. Type ***osub h7g5-web/bnd.bnd*** and paste the following code.

	```
	Bundle-Name: Liferay H7G5 Web
	Bundle-SymbolicName: com.liferay.h7g5.web
	Bundle-Version: 1.0.0
	```

1. Type ***osub h7g5-web/build.gradle*** and paste the following code.

	```
	dependencies {
		compileOnly group: "com.liferay.portal", name: "release.dxp.api", version: "2025.q1.2"
		compileOnly project(":h7g5-api")
	}
	```

1. Type ***./gradlew deploy -Ddeploy.docker.container.id=ephesians-liferay***.

	```
	2021-03-25 11:46:37.195 INFO  [fileinstall-directory-watcher][BundleStartStopLogger:46] STARTED com.liferay.h7g5.web_1.0.0 [1357]
	Constructing H7G5Portlet
	```

1. Go to http://localhost:8080 and add ***H7G5*** to a widget page.

	```
	Invoking H7G5Portlet#doView
	```

1. Type ***osub h7g5-web/src/main/java/com/liferay/h7g5/web/internal/portlet/H7G5Portlet.java*** and paste the following code.

	```
	package com.liferay.h7g5.web.internal.portlet;

	import com.liferay.h7g5.model.H7G5Folder;
	import com.liferay.h7g5.service.H7G5FolderLocalService;
	import com.liferay.portal.kernel.util.StringUtil;

	import java.io.IOException;
	import java.io.PrintWriter;

	import javax.portlet.GenericPortlet;
	import javax.portlet.Portlet;
	import javax.portlet.RenderRequest;
	import javax.portlet.RenderResponse;

	import org.osgi.service.component.annotations.Component;
	import org.osgi.service.component.annotations.Reference;

	@Component(
		property = {
			"com.liferay.portlet.display-category=category.sample",
			"javax.portlet.display-name=H7G5"
		},
		service = Portlet.class
	)
	public class H7G5Portlet extends GenericPortlet {

		public H7G5Portlet() {
			System.out.println("Constructing H7G5Portlet");
		}

		@Override
		protected void doView(
				RenderRequest renderRequest, RenderResponse renderResponse)
			throws IOException {

			System.out.println("Invoking H7G5Portlet#doView");

			PrintWriter printWriter = renderResponse.getWriter();

			printWriter.println("Hello, H7G5!");

			System.out.println(
				"There are " + _h7G5FolderLocalService.getH7G5FoldersCount() +
					" folders.");

			H7G5Folder h7g5Folder = _h7G5FolderLocalService.createH7G5Folder(
				System.currentTimeMillis());

			h7g5Folder.setDescription(StringUtil.randomString());
			h7g5Folder.setName(StringUtil.randomString());

			_h7G5FolderLocalService.addH7G5Folder(h7g5Folder);

			System.out.println(
				"After adding a new folder, there are now " +
					_h7G5FolderLocalService.getH7G5FoldersCount() + " folders.");
		}

		@Reference
		private H7G5FolderLocalService _h7G5FolderLocalService;

	}
	```

1. Type ***./gradlew classes***. Fix the compile errors.

1. Type ***./gradlew deploy -Ddeploy.docker.container.id=ephesians-liferay***.

	```
	2021-03-25 12:13:09.889 INFO  [fileinstall-directory-watcher][BundleStartStopLogger:49] STOPPED com.liferay.h7g5.web_1.0.0 [1357]
	2021-03-25 12:13:09.939 INFO  [Refresh Thread: Equinox Container: 743d7fd3-e7c1-438a-9dee-b8ecf6e55aa2][BundleStartStopLogger:46] STARTED com.liferay.h7g5.web_1.0.0 [1357]
	Constructing H7G5Portlet
	```	

1. Go to http://localhost:8080 and refresh the page.

	```
	java.lang.NullPointerException
		at com.liferay.h7g5.service.base.H7G5FolderLocalServiceBaseImpl.getH7G5FoldersCount(H7G5FolderLocalServiceBaseImpl.java:346)
		at com.liferay.portal.spring.aop.AopMethodInvocationImpl.proceed(AopMethodInvocationImpl.java:50)
		at com.liferay.portal.spring.transaction.TransactionInterceptor.invoke(TransactionInterceptor.java:69)
		at com.liferay.portal.spring.aop.AopMethodInvocationImpl.proceed(AopMethodInvocationImpl.java:57)
		at com.liferay.change.tracking.internal.aop.CTTransactionAdvice.invoke(CTTransactionAdvice.java:80)
		at com.liferay.portal.spring.aop.AopMethodInvocationImpl.proceed(AopMethodInvocationImpl.java:57)
		at com.liferay.portal.spring.aop.AopInvocationHandler.invoke(AopInvocationHandler.java:49)
	```

	Open up ***H7G5FolderLocalServiceBaseImpl.java***. That is the base implementation wired to the ***H7G5Portlet*** via the following reference.

	```
	@Reference
	private H7G5FolderLocalService _h7G5FolderLocalService;
	```

	You can trace the exception to the field ***h7g5FolderPersistence*** being null.

	The field ***h7g5FolderPersistence*** in ***H7G5FolderLocalServiceBaseImpl.java*** should be wired by the following annotation, but is not.

	```
	@Reference
	protected H7G5FolderPersistence h7g5FolderPersistence;
	```

1. Add the following line to ***h7g5-service/bnd.bnd***.

	```
	-dsannotations-options: inherit
	```

	The ***-dsannotations-options:*** property ensures that references are inherited. Since ***H7G5Portlet*** references ***H7G5FolderLocalService***, and the base implementation for ***H7G5FolderLocalService*** is ***H7G5FolderLocalServiceBaseImpl***, and that base implementation references ***H7G5FolderPersistence***, it too is now wired once the property is set to ***inherit***.

	Type ***./gradlew deploy -Ddeploy.docker.container.id=ephesians-liferay***.

1. Go to http://localhost:8080 and refresh the page. Look at the Liferay console. Refresh the page again a few times.

	```
	Invoking H7G5Portlet#doView
	There are 2 folders.
	After adding a new folder, there are now 3 folders.
	Invoking H7G5Portlet#doView
	There are 3 folders.
	After adding a new folder, there are now 4 folders.
	```

1. Type ***select * from OHQIWTSFHL_H7G5Folder;***.

## Local and Remote Services

1. The following XML told Service Builder to generate local and remote services for ***H7G5Folder***.

	```
	<entity local-service="true" name="H7G5Folder" remote-service="true">
	```

	A local service should never check for permissions and can only be invoked by other OSGi modules.

	A remote service may check for permissions and can be invoked by other OSGi modules and via [JSONWS](https://help.liferay.com/hc/en-us/articles/360017899652-Invoking-JSON-Web-Services).

1. Open up ***H7G5FolderLocalService.java*** and ***H7G5FolderLocalServiceImpl.java***.

	These files were generated because the attribute ***local-service*** was set to ***true***.

	The file ***H7G5FolderLocalService.java*** contains the ***@generated*** annotation. This file is always regenerated when you run the ***buildService*** task. Do not modify this file.

	The file ***H7G5FolderLocalServiceImpl.java*** does not contain the ***@generated*** annotation. This file is only generated if it does not already exist. Later, we will add custom methods to this file.

1. Open up ***H7G5FolderService.java*** and ***H7G5FolderServiceImpl.java***.

	These files were generated because the attribute ***remote-service*** was set to ***true***.

	The file ***H7G5FolderService.java*** contains the ***@generated*** annotation. This file is always regenerated when you run the ***buildService*** task. Do not modify this file.

	The file ***H7G5FolderServiceImpl.java*** does not contain the ***@generated*** annotation. This file is only generated if it does not already exist. Later, we will add custom methods to this file.

1. Add the following code to ***H7G5FolderServiceImpl.java***.

	```
	public H7G5Folder addMyCustomH7G5Folder(String description, String name) {
		System.out.println(
			"Invoking H7G5FolderServiceImpl#addMyCustomH7G5Folder(" +
				description + ", " + name + ")");

		H7G5Folder h7g5Folder = h7g5FolderLocalService.createH7G5Folder(
			System.currentTimeMillis());

		h7g5Folder.setDescription(description);
		h7g5Folder.setName(name);

		h7g5FolderLocalService.addH7G5Folder(h7g5Folder);

		return h7g5Folder;
	}
	```

1. Open up ***H7G5FolderService.java*** and notice that there is no code for ***addMyCustomH7G5Folder***.

1. Type ***./gradlew classes***. Fix the compile errors.

1. Type ***./gradlew h7g5-service:buildService***.

	```
	Building H7G5Entry
	Building H7G5Folder
	Writing ../h7g5-api/src/main/java/com/liferay/h7g5/service/H7G5FolderService.java
	Writing ../h7g5-api/src/main/java/com/liferay/h7g5/service/H7G5FolderServiceUtil.java
	Writing ../h7g5-api/src/main/java/com/liferay/h7g5/service/H7G5FolderServiceWrapper.java
	Writing src/main/java/com/liferay/h7g5/service/http/H7G5FolderServiceHttp.java
	Writing src/main/java/com/liferay/h7g5/service/http/H7G5FolderServiceSoap.java
	Writing src/main/resources/service.properties
	```

1. Open up ***H7G5FolderService.java*** and notice that it now has code for ***addMyCustomH7G5Folder***.

1. Go to http://localhost:8080/api/jsonws. Under ***Context Name***, look for ***ohqiwtsfhl***. The long namespace was specified in the following block in service.xml.

	```
	<namespace>OHQIWTSFHL</namespace>
	```

	There is no mention of OHQIWTSFHL because the deployed ***com.liferay.h7g5.service** does not yet contain remote services.

1. Type ***./gradlew deploy -Ddeploy.docker.container.id=ephesians-liferay***.

1. Go to http://localhost:8080/api/jsonws. Under ***Context Name***, go to for ***ohqiwtsfhl***.

	1. Follow the [Invoking JSON Web Services](https://help.liferay.com/hc/en-us/articles/360017899652-Invoking-JSON-Web-Services) tutorial.

	1. Use curl to invoke JSONWS.
	
		```
		curl http://localhost:8080/api/jsonws/ohqiwtsfhl.h7g5folder/add-my-custom-h7-g5-folder \
		  -d description="This is where I store all my vacation photos." \
		  -d name="Vacation Photos" \
		  -u test@liferay.com:test
		```

		A JSON representation of H7G5Folder is returned.

		```
		{"companyId":"20097","createDate":1616680001273,"description":"This is where I store all my vacation photos.","groupId":"0","h7g5FolderId":"1616680001273","modifiedDate":1616680001273,"name":"Vacation Photos","userId":"0","userName":""}
		```

		This is printed on the Liferay console.

		```
		Invoking H7G5FolderServiceImpl#addMyCustomH7G5Folder(This is where I store all my vacation photos., Vacation Photos)
		```

	1. Type ***select * from OHQIWTSFHL_H7G5Folder;***.

1. Add the following code to ***H7G5FolderServiceImpl.java***.

	```
	public H7G5Folder addMyCustomH7G5FolderWithPermissionCheck(
			String description, String name)
		throws PortalException {

		User user = getUser();

		if (!Objects.equals(user.getEmailAddress(), "test@liferay.com")) {
			throw new PrincipalException("You are not test@liferay.com");
		}

		return addMyCustomH7G5Folder(description, name);
	}
	```

	1. Fix the compile errors. Regenerate. Deploy it to Liferay.

	1. Use a curl command to invoke ***addMyCustomH7G5FolderWithPermissionCheck*** as ***test@liferay.com*** and see that it works. Add a new user to Liferay. Run the curl command as the other user and verify that you see the exception message `You are not test@liferay.com`.

## Liferay and Service Builder

1. Go to ***/home/me/dev/projects/liferay-portal***.

1. Type ***g ls-files \*\*service.xml***. Liferay is made up of many service.xml files. How many?

1. Type ***g ls-files \*\*service.xml | wc -l***

1. Type ***osub \`g ls-files \*\*service.xml\`*** to open up every service.xml file. Scan through them.

## Mastering Service Builder

1. Contrast [BookmarksEntryServiceImpl.java](https://github.com/brianchandotcom/liferay-portal/blob/master/modules/apps/bookmarks/bookmarks-service/src/main/java/com/liferay/bookmarks/service/impl/BookmarksEntryServiceImpl.java) with [BookmarksEntryLocalServiceImpl.java](https://github.com/brianchandotcom/liferay-portal/blob/master/modules/apps/bookmarks/bookmarks-service/src/main/java/com/liferay/bookmarks/service/impl/BookmarksEntryLocalServiceImpl.java).

	Notice how CRUD logic is concentrated in the local service.

	```
	public BookmarksEntry addEntry(
			long userId, long groupId, long folderId, String name, String url,
			String description, ServiceContext serviceContext)
		throws PortalException {

		// Entry

		User user = userLocalService.getUser(userId);

		if (Validator.isNull(name)) {
			name = url;
		}

		_validate(url);

		long entryId = counterLocalService.increment();

		BookmarksEntry entry = bookmarksEntryPersistence.create(entryId);

		entry.setUuid(serviceContext.getUuid());
		entry.setGroupId(groupId);
		entry.setCompanyId(user.getCompanyId());
		entry.setUserId(user.getUserId());
		entry.setUserName(user.getFullName());
		entry.setFolderId(folderId);
		entry.setTreePath(entry.buildTreePath());
		entry.setName(name);
		entry.setUrl(url);
		entry.setDescription(description);
		entry.setExpandoBridgeAttributes(serviceContext);

		entry = bookmarksEntryPersistence.update(entry);

		// Resources

		resourceLocalService.addModelResources(entry, serviceContext);

		// Asset

		updateAsset(
			userId, entry, serviceContext.getAssetCategoryIds(),
			serviceContext.getAssetTagNames(),
			serviceContext.getAssetLinkEntryIds(),
			serviceContext.getAssetPriority());

		// Social

		JSONObject extraDataJSONObject = JSONUtil.put("title", entry.getName());

		socialActivityLocalService.addActivity(
			userId, groupId, BookmarksEntry.class.getName(), entryId,
			BookmarksActivityKeys.ADD_ENTRY, extraDataJSONObject.toString(), 0);

		// Subscriptions

		_notifySubscribers(userId, entry, serviceContext);

		return entry;
	}
	```

	Notice how the remote service simply calls the local service, but adds permission checking.

	```
	public BookmarksEntry addEntry(
			long groupId, long folderId, String name, String url,
			String description, ServiceContext serviceContext)
		throws PortalException {

		ModelResourcePermissionUtil.check(
			_bookmarksFolderModelResourcePermission, getPermissionChecker(),
			groupId, folderId, ActionKeys.ADD_ENTRY);

		return bookmarksEntryLocalService.addEntry(
			getUserId(), groupId, folderId, name, url, description,
			serviceContext);
	}
	```

	For now, ignore ModelResourcePermissionUtil and use a simple email address permission check in your code.

	The main thing to understand is that local services contain CRUD logic while remote services contain permission logic.

1. Modify H7G5EntryLocalServiceImpl to add getter methods that invoke the generated finder methods.

	```
	<!-- Finder methods -->

	<finder name="H7G5FolderId" return-type="Collection">
		<finder-column name="h7g5FolderId" />
	</finder>
	<finder name="Key" return-type="H7G5Entry">
		<finder-column name="key" />
	</finder>
	<finder name="Name" return-type="Collection">
		<finder-column name="name" />
	</finder>
	<finder name="H_D_N" return-type="H7G5Entry">
		<finder-column name="h7g5FolderId" />
		<finder-column name="description" />
		<finder-column name="name" />
	</finder>
	```

	Modify H7G5EntryServiceImpl to call H7G5EntryLocalServiceImpl. Add a simple email address permission check.

1. Use H7G5Portlet to call H7G5EntryServiceImpl.

1. Use curl to call H7G5EntryServiceImpl.

1. Send a pull request to ***brianchandotcom*** with your custom Java files (not the generated Java files) and a shell script containing all your curl commands.

1. Explore [DynamicQuery](https://help.liferay.com/hc/en-us/articles/360017882032-Dynamic-Query) to replace the usage of the finder methods above. Finder methods create unique SQL indexes that perform better than dynamic queries. Dynamic queries are useful because they provide more ways for developers to retrieve data.

1. Send a pull request to ***brianchandotcom*** with your custom Java files (not the generated Java files) and a shell script containing all your curl commands.
