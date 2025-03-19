/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

 package com.liferay.h7g5.service.impl;

 import com.liferay.h7g5.model.H7G5Entry;
 import com.liferay.h7g5.service.base.H7G5EntryLocalServiceBaseImpl;
 import com.liferay.portal.aop.AopService;
 
 import com.liferay.portal.kernel.exception.PortalException;
 import org.osgi.service.component.annotations.Component;
 
 import java.util.List;
 
 /**
  * @author Brian Wing Shun Chan
  */
 @Component(
     property = "model.class.name=com.liferay.h7g5.model.H7G5Entry",
     service = AopService.class
 )
 public class H7G5EntryLocalServiceImpl extends H7G5EntryLocalServiceBaseImpl {
     public List<H7G5Entry> getMyCustomH7G5FolderId(long h7g5FolderId) {
         System.out.println("Invoking h7g5FolderId(" + h7g5FolderId + ")");
         return h7g5EntryPersistence.findByH7G5FolderId(h7g5FolderId);
     }
 
     public H7G5Entry getH7G5EntryKey(String key) throws PortalException {
         System.out.println("Invoking H7G5EntryLocalServiceImpl#getH7G5Entry(" + key + ")");
         return h7g5EntryPersistence.findByPrimaryKey(key);
     }
 
     public H7G5Entry getH7G5EntryName(String name) throws PortalException {
         System.out.println("Invoking H7G5EntryLocalServiceImpl#getMyCustomH7G5Name(" + name + ")");
         return (H7G5Entry) h7g5EntryPersistence.findByName(name);
     }
 
     public List<H7G5Entry> getH7G5ByHDN(long h7g5folderId, String description, String name) throws PortalException {
         System.out.println("Invoking H7G5EntryLocalServiceImpl#getH7G5ByHDN(" + h7g5folderId  + ", "  +  description  + ", " +  name + ")");
         return (List<H7G5Entry>) h7g5EntryPersistence.findByH_D_N(h7g5folderId, description, name);
     }
 
 }