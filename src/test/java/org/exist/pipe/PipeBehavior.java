/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2013 The eXist Project
 *  http://exist-db.org
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *  $Id$
 */
package org.exist.pipe;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.exist.Database;
import org.exist.EXistException;
import org.exist.backup.BackupHandler;
import org.exist.backup.RestoreHandler;
import org.exist.collections.CollectionConfigurationManager;
import org.exist.collections.triggers.CollectionTrigger;
import org.exist.collections.triggers.DocumentTrigger;
import org.exist.config.ConfigurationException;
import org.exist.debuggee.Debuggee;
import org.exist.dom.DocumentImpl;
import org.exist.dom.SymbolTable;
import org.exist.indexing.IndexManager;
import org.exist.numbering.NodeIdFactory;
import org.exist.plugin.PluginsManager;
import org.exist.scheduler.Scheduler;
import org.exist.security.Account;
import org.exist.security.AuthenticationException;
import org.exist.security.Group;
import org.exist.security.PermissionDeniedException;
import org.exist.security.PermissionFactory;
import org.exist.security.SchemaType;
import org.exist.security.SecurityManager;
import org.exist.security.Session;
import org.exist.security.Subject;
import org.exist.security.internal.aider.GroupAider;
import org.exist.security.realm.Realm;
import org.exist.security.xacml.ExistPDP;
import org.exist.storage.BrokerPool;
import org.exist.storage.CacheManager;
import org.exist.storage.DBBroker;
import org.exist.storage.NotificationService;
import org.exist.storage.ProcessMonitor;
import org.exist.storage.txn.TransactionManager;
import org.exist.util.Configuration;
import org.exist.xquery.PerformanceStats;
import org.junit.Test;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class PipeBehavior {

    @Test
    public void test() throws IOException {
        
        PluginsManager pm = new FakePluginsManager();
        
        PermissionFactory.sm = pm.getDatabase().getSecurityManager();
        
        final Pipes _ = new Pipes(pm);
        
        final ObjectPipe<String> pipe = (ObjectPipe<String>) _.make();

        Runnable tOne = new Runnable(){
            public void run() {
                try {
                    for (int i = 0; i < 50; i++)
                        pipe.write("Shalom" + i + "\n");
                    
                    Thread.sleep(300);
                    
                    pipe.write("\n");
                    
                    for (int i = 0; i < 50; i++)
                        pipe.write("Shalom" + i + "\n");
                    
                    pipe.write("\n");
                    
                    Thread.sleep(3000);
                    
                    for (int i = 0; i < 50; i++)
                        pipe.write("Shalom" + i + "\n");
                
                } catch (Exception e) {
                    fail(e.getMessage());
                }
            }
        };

        Runnable tTwo = new Runnable() {
            public void run() {
                try {
                    String msg;
                    while ((msg = pipe.read()) != null) {
                        System.out.print(msg);
                    }
                } catch (IOException e) {
                    fail(e.getMessage());
                }
            }
        };

        ThreadPoolExecutor exec = new ThreadPoolExecutor(600, 600,
                3L, TimeUnit.MINUTES,
                new SynchronousQueue<Runnable>());
        exec.setRejectedExecutionHandler(
                new ThreadPoolExecutor.CallerRunsPolicy());

        for (int i = 0; i < 500; i++) exec.execute(tOne);

        exec.execute(tTwo);

//        fail("Not yet implemented");
    }

    class FakePluginsManager implements PluginsManager {
        
        Group group = new GroupAider("test");
        Subject subject = new Subject() {
            @Override
            public Group addGroup(String name) throws PermissionDeniedException {
                throw new IllegalAccessError();
            }

            @Override
            public Group addGroup(Group group) throws PermissionDeniedException {
                throw new IllegalAccessError();
            }

            @Override
            public void setPrimaryGroup(Group group) throws PermissionDeniedException {
                throw new IllegalAccessError();
            }

            @Override
            public void remGroup(String group) throws PermissionDeniedException {
                throw new IllegalAccessError();
            }

            @Override
            public String[] getGroups() {
                throw new IllegalAccessError();
            }

            @Override
            public int[] getGroupIds() {
                return new int[] {group.getId()};
            }

            @Override
            public boolean hasDbaRole() {
                return false;
            }

            @Override
            public String getPrimaryGroup() {
                return group.getName();
            }

            @Override
            public Group getDefaultGroup() {
                return group;
            }

            @Override
            public boolean hasGroup(String group) {
                throw new IllegalAccessError();
            }

            @Override
            public void setPassword(String passwd) {
                throw new IllegalAccessError();
            }

            @Override
            public Realm getRealm() {
                throw new IllegalAccessError();
            }

            @Override
            public String getPassword() {
                throw new IllegalAccessError();
            }

            @Override
            @Deprecated
            public String getDigestPassword() {
                throw new IllegalAccessError();
            }

            @Override
            @Deprecated
            public void setGroups(String[] groups) {
                throw new IllegalAccessError();
            }

            @Override
            public String getUsername() {
                throw new IllegalAccessError();
            }

            @Override
            public boolean isAccountNonExpired() {
                throw new IllegalAccessError();
            }

            @Override
            public boolean isAccountNonLocked() {
                throw new IllegalAccessError();
            }

            @Override
            public boolean isCredentialsNonExpired() {
                throw new IllegalAccessError();
            }

            @Override
            public boolean isEnabled() {
                return false;
            }

            @Override
            public void assertCanModifyAccount(Account user) throws PermissionDeniedException {
                throw new IllegalAccessError();
            }

            @Override
            public int getUserMask() {
                throw new IllegalAccessError();
            }

            @Override
            public void setUserMask(int umask) {
                throw new IllegalAccessError();
            }

            @Override
            public int getId() {
                return 1;
            }

            @Override
            public String getRealmId() {
                throw new IllegalAccessError();
            }

            @Override
            public void save() throws ConfigurationException, PermissionDeniedException {
                throw new IllegalAccessError();
            }

            @Override
            public void save(DBBroker broker) throws ConfigurationException, PermissionDeniedException {
                throw new IllegalAccessError();
            }

            @Override
            public void setMetadataValue(SchemaType schemaType, String value) {
                throw new IllegalAccessError();
            }

            @Override
            public String getMetadataValue(SchemaType schemaType) {
                throw new IllegalAccessError();
            }

            @Override
            public Set<SchemaType> getMetadataKeys() {
                throw new IllegalAccessError();
            }

            @Override
            public void clearMetadata() {
                throw new IllegalAccessError();
            }

            @Override
            public String getName() {
                throw new IllegalAccessError();
            }

            @Override
            public boolean isConfigured() {
                throw new IllegalAccessError();
            }

            @Override
            public org.exist.config.Configuration getConfiguration() {
                throw new IllegalAccessError();
            }

            @Override
            public void setEnabled(boolean enabled) {
                throw new IllegalAccessError();
            }

            @Override
            public boolean authenticate(Object credentials) {
                throw new IllegalAccessError();
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public boolean isExternallyAuthenticated() {
                throw new IllegalAccessError();
            }

            @Override
            public String getSessionId() {
                throw new IllegalAccessError();
            }

            @Override
            public Session getSession() {
                throw new IllegalAccessError();
            }
            
        };

        final Database db = new Database() {
            
            @Override
            public String getId() {
                throw new IllegalAccessError();
            }

            @Override
            public void addObserver(Observer o) {
                throw new IllegalAccessError();
            }

            @Override
            public SecurityManager getSecurityManager() {
                return sm;
            }

            @Override
            public IndexManager getIndexManager() {
                throw new IllegalAccessError();
            }

            @Override
            public TransactionManager getTransactionManager() {
                throw new IllegalAccessError();
            }

            @Override
            public CacheManager getCacheManager() {
                throw new IllegalAccessError();
            }

            @Override
            public Scheduler getScheduler() {
                throw new IllegalAccessError();
            }

            @Override
            public void shutdown() {
                throw new IllegalAccessError();
            }

            @Override
            public Subject getSubject() {
                return subject;
            }

            @Override
            public boolean setSubject(Subject subject) {
                throw new IllegalAccessError();
            }

            @Override
            public DBBroker getBroker() throws EXistException {
                throw new IllegalAccessError();
            }

            @Override
            public DBBroker authenticate(String username, Object credentials) throws AuthenticationException {
                throw new IllegalAccessError();
            }

            @Override
            public DBBroker get(Subject subject) throws EXistException {
                throw new IllegalAccessError();
            }

            @Override
            public DBBroker getActiveBroker() {
                throw new IllegalAccessError();
            }

            @Override
            public void release(DBBroker broker) {
                throw new IllegalAccessError();
            }

            @Override
            public int countActiveBrokers() {
                throw new IllegalAccessError();
            }

            @Override
            public Debuggee getDebuggee() {
                throw new IllegalAccessError();
            }

            @Override
            public PerformanceStats getPerformanceStats() {
                throw new IllegalAccessError();
            }

            @Override
            public Configuration getConfiguration() {
                throw new IllegalAccessError();
            }

            @Override
            public NodeIdFactory getNodeFactory() {
                throw new IllegalAccessError();
            }

            @Override
            public File getStoragePlace() {
                throw new IllegalAccessError();
            }

            @Override
            public CollectionConfigurationManager getConfigurationManager() {
                throw new IllegalAccessError();
            }

            @Override
            public Collection<DocumentTrigger> getDocumentTriggers() {
                throw new IllegalAccessError();
            }

            @Override
            public DocumentTrigger getDocumentTrigger() {
                throw new IllegalAccessError();
            }

            @Override
            public Collection<CollectionTrigger> getCollectionTriggers() {
                throw new IllegalAccessError();
            }

            @Override
            public CollectionTrigger getCollectionTrigger() {
                throw new IllegalAccessError();
            }

            @Override
            public ProcessMonitor getProcessMonitor() {
                throw new IllegalAccessError();
            }

            @Override
            public boolean isReadOnly() {
                throw new IllegalAccessError();
            }

            @Override
            public NotificationService getNotificationService() {
                throw new IllegalAccessError();
            }

            @Override
            public PluginsManager getPluginsManager() {
                return FakePluginsManager.this;
            }

            @Override
            public SymbolTable getSymbols() {
                throw new IllegalAccessError();
            }
        };
        
        SecurityManager sm = new SecurityManager() {

            @Override
            public boolean isConfigured() {
                throw new IllegalAccessError();
            }

            @Override
            public org.exist.config.Configuration getConfiguration() {
                throw new IllegalAccessError();
            }

            @Override
            public void attach(BrokerPool pool, DBBroker sysBroker) throws EXistException {
                throw new IllegalAccessError();
            }

            @Override
            public Database getDatabase() {
                return db;
            }

            @Override
            public boolean isXACMLEnabled() {
                throw new IllegalAccessError();
            }

            @Override
            public ExistPDP getPDP() {
                throw new IllegalAccessError();
            }

            @Override
            public Account getAccount(int id) {
                throw new IllegalAccessError();
            }

            @Override
            public boolean hasAccount(String name) {
                throw new IllegalAccessError();
            }

            @Override
            public Account addAccount(Account user) throws PermissionDeniedException, EXistException, ConfigurationException {
                throw new IllegalAccessError();
            }

            @Override
            public Account addAccount(DBBroker broker, Account account) throws PermissionDeniedException, EXistException, ConfigurationException {
                throw new IllegalAccessError();
            }

            @Override
            public boolean deleteAccount(String name) throws PermissionDeniedException, EXistException, ConfigurationException {
                throw new IllegalAccessError();
            }

            @Override
            public boolean deleteAccount(Account account) throws PermissionDeniedException, EXistException, ConfigurationException {
                throw new IllegalAccessError();
            }

            @Override
            public boolean updateAccount(Account account) throws PermissionDeniedException, EXistException, ConfigurationException {
                throw new IllegalAccessError();
            }

            @Override
            public boolean updateGroup(Group group) throws PermissionDeniedException, EXistException, ConfigurationException {
                throw new IllegalAccessError();
            }

            @Override
            public Account getAccount(String name) {
                throw new IllegalAccessError();
            }

            @Override
            public Group addGroup(Group group) throws PermissionDeniedException, EXistException, ConfigurationException {
                throw new IllegalAccessError();
            }

            @Override
            @Deprecated
            public void addGroup(String group) throws PermissionDeniedException, EXistException, ConfigurationException {
                throw new IllegalAccessError();
            }

            @Override
            public boolean hasGroup(String name) {
                throw new IllegalAccessError();
            }

            @Override
            public boolean hasGroup(Group group) {
                throw new IllegalAccessError();
            }

            @Override
            public Group getGroup(String name) {
                throw new IllegalAccessError();
            }

            @Override
            public Group getGroup(int gid) {
                throw new IllegalAccessError();
            }

            @Override
            public boolean deleteGroup(String name) throws PermissionDeniedException, EXistException {
                throw new IllegalAccessError();
            }

            @Override
            public boolean hasAdminPrivileges(Account user) {
                throw new IllegalAccessError();
            }

            @Override
            public Subject authenticate(String username, Object credentials) throws AuthenticationException {
                throw new IllegalAccessError();
            }

            @Override
            public Subject getSystemSubject() {
                throw new IllegalAccessError();
            }

            @Override
            public Subject getGuestSubject() {
                throw new IllegalAccessError();
            }

            @Override
            public Group getDBAGroup() {
                throw new IllegalAccessError();
            }

            @Override
            public List<Account> getGroupMembers(String groupName) {
                throw new IllegalAccessError();
            }

            @Override
            @Deprecated
            public Collection<Account> getUsers() {
                throw new IllegalAccessError();
            }

            @Override
            @Deprecated
            public Collection<Group> getGroups() {
                throw new IllegalAccessError();
            }

            @Override
            public void registerSession(Session session) {
                throw new IllegalAccessError();
            }

            @Override
            public Subject getSubjectBySessionId(String sessionid) {
                throw new IllegalAccessError();
            }

            @Override
            public void addGroup(int id, Group group) {
                throw new IllegalAccessError();
            }

            @Override
            public void addUser(int id, Account account) {
                throw new IllegalAccessError();
            }

            @Override
            public boolean hasGroup(int id) {
                throw new IllegalAccessError();
            }

            @Override
            public boolean hasUser(int id) {
                throw new IllegalAccessError();
            }

            @Override
            public List<String> findUsernamesWhereNameStarts(String startsWith) {
                throw new IllegalAccessError();
            }

            @Override
            public List<String> findUsernamesWhereUsernameStarts(String startsWith) {
                throw new IllegalAccessError();
            }

            @Override
            public List<String> findAllGroupNames() {
                throw new IllegalAccessError();
            }

            @Override
            public List<String> findAllUserNames() {
                throw new IllegalAccessError();
            }

            @Override
            public List<String> findGroupnamesWhereGroupnameStarts(String startsWith) {
                throw new IllegalAccessError();
            }

            @Override
            public List<String> findAllGroupMembers(String groupName) {
                throw new IllegalAccessError();
            }

            @Override
            public void processPramatter(DBBroker broker, DocumentImpl document) throws ConfigurationException {
                throw new IllegalAccessError();
            }

            @Override
            public void processPramatterBeforeSave(DBBroker broker, DocumentImpl document) throws ConfigurationException {
                throw new IllegalAccessError();
            }

            @Override
            public String getAuthenticationEntryPoint() {
                throw new IllegalAccessError();
            }

            @Override
            public List<String> findGroupnamesWhereGroupnameContains(String fragment) {
                throw new IllegalAccessError();
            }

            @Override
            public List<String> findUsernamesWhereNamePartStarts(String startsWith) {
                throw new IllegalAccessError();
            }

            @Override
            public Subject getCurrentSubject() {
                return subject;
            }
        };
        
        @Override
        public RestoreHandler getRestoreHandler() {
            throw new IllegalAccessError();
        }
        
        @Override
        public Database getDatabase() {
            return db;
        }
        
        public SecurityManager getSecurityManager() {
            return sm;
        }

        @Override
        public void addPlugin(String className) {
            throw new IllegalAccessError();
        }

        @Override
        public BackupHandler getBackupHandler(Logger logger) {
            throw new IllegalAccessError();
        }
    };
}
