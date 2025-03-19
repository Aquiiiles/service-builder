/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

 package com.liferay.h7g5.service.impl;

 import com.liferay.h7g5.model.H7G5Entry;
 import com.liferay.h7g5.service.base.H7G5EntryServiceBaseImpl;
 import com.liferay.portal.aop.AopService;
 
 import com.liferay.portal.kernel.exception.PortalException;
 import com.liferay.portal.kernel.model.User;
 import com.liferay.portal.kernel.security.auth.PrincipalException;
 import org.osgi.service.component.annotations.Component;
 
 import java.util.List;
 import java.util.Objects;
 
 /**
  * @author Brian Wing Shun Chan
  */
 @Component(
     property = {
         "json.web.service.context.name=ohqiwtsfhl",
         "json.web.service.context.path=H7G5Entry"
     },
     service = AopService.class
 )
 public class H7G5EntryServiceImpl extends H7G5EntryServiceBaseImpl {
     public List<H7G5Entry> getMyCustomH7G5FolderId(long h7g5FolderId)
             throws PortalException {
 
         User user = getUser();
 
         if (!Objects.equals(user.getEmailAddress(), "test@liferay.com")) {
             throw new PrincipalException("You are not test@liferay.com");
         }
 
         return h7g5EntryLocalService.getMyCustomH7G5FolderId(h7g5FolderId);
     }
 
     public H7G5Entry getH7G5EntryKey(String key)
             throws PortalException {
         User user = getUser();
 
         if (!Objects.equals(user.getEmailAddress(), "test@liferay.com")) {
             throw new PrincipalException("You are not test@liferay.com");
         }
 
         return h7g5EntryLocalService.getH7G5EntryKey(key);
     }
     public H7G5Entry getH7G5EntryName(String name)
             throws PortalException {
         User user = getUser();
 
         if (!Objects.equals(user.getEmailAddress(), "test@liferay.com")) {
             throw new PrincipalException("You are not test@liferay.com");
         }
 
         return h7g5EntryLocalService.getH7G5EntryName(name);
     }
 
     public H7G5Entry getH7G5ByHDN(long h7g5folderId, String description, String name)
             throws PortalException {
         User user = getUser();
 
         if (!Objects.equals(user.getEmailAddress(), "test@liferay.com")) {
             throw new PrincipalException("You are not test@liferay.com");
         }
 
         return (H7G5Entry) h7g5EntryLocalService.getH7G5ByHDN(h7g5folderId, description, name);
     }
 }