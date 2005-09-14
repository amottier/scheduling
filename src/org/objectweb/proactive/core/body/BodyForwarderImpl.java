/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.body;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.ft.internalmsg.FTMessage;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.component.request.Shortcut;
import org.objectweb.proactive.core.exceptions.NonFunctionalException;
import org.objectweb.proactive.core.exceptions.manager.NFEListener;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeForwarderImpl;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.ext.security.Communication;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;
import org.objectweb.proactive.ext.security.SecurityContext;
import org.objectweb.proactive.ext.security.crypto.AuthenticationException;
import org.objectweb.proactive.ext.security.crypto.ConfidentialityTicket;
import org.objectweb.proactive.ext.security.crypto.KeyExchangeException;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;


public class BodyForwarderImpl implements UniversalBodyForwarder {

    /** Cached bodies, key = BodyID */
    private HashMap bodies = new HashMap();

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Creates a new AbstractBody.
     * Used for serialization.
     */
    public BodyForwarderImpl() {
    }

    public void createShortcut(UniqueID id, Shortcut shortcut)
        throws IOException {
    }

    public synchronized void add(UniversalBody rBody) {
        if (!bodies.containsKey(rBody.getID())) {
            bodies.put(rBody.getID(), rBody);
        }
    }

    public void disableAC(UniqueID id) throws IOException {
        try {
            BodyAdapter rbody = (BodyAdapter) bodies.get(id);
            if (rbody != null) {
                rbody.disableAC();
            } else {
                throw new IOException("No BodyAdapter associated to id=" + id);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    public void enableAC(UniqueID id) throws IOException {
        try {
            BodyAdapter rbody = (BodyAdapter) bodies.get(id);
            if (rbody != null) {
                rbody.enableAC();
            } else {
                throw new IOException("No BodyAdapter associated to id=" + id);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    public X509Certificate getCertificate(UniqueID id)
        throws SecurityNotAvailableException, IOException {
        try {
            BodyAdapter rbody = (BodyAdapter) bodies.get(id);
            if (rbody != null) {
                return rbody.getCertificate();
            } else {
                throw new IOException("No BodyAdapter associated to id=" + id);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    public byte[] getCertificateEncoded(UniqueID id)
        throws SecurityNotAvailableException, IOException {
        try {
            BodyAdapter rbody = (BodyAdapter) bodies.get(id);
            if (rbody != null) {
                return rbody.getCertificateEncoded();
            } else {
                throw new IOException("No BodyAdapter associated to id=" + id);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    public ArrayList getEntities(UniqueID id)
        throws SecurityNotAvailableException, IOException {
        try {
            BodyAdapter rbody = (BodyAdapter) bodies.get(id);
            if (rbody != null) {
                return rbody.getEntities();
            } else {
                throw new IOException("No BodyAdapter associated to id=" + id);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    public UniqueID getID(UniqueID id) {
        try {
            BodyAdapter rbody = (BodyAdapter) bodies.get(id);
            if (rbody != null) {
                return rbody.getID();
            } else {
                bodyLogger.info(
                    "Cannot retieve associated BodyAdapter: Invalid ID " + id);
                return null;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    public String getNodeURL(UniqueID id) {
        try {
            BodyAdapter rbody = (BodyAdapter) bodies.get(id);
            if (rbody != null) {
                return rbody.getNodeURL();
            } else {
                bodyLogger.info(
                    "Cannot retieve associated BodyAdapter: Invalid ID " + id);
                return null;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    public SecurityContext getPolicy(UniqueID id,
        SecurityContext securityContext)
        throws SecurityNotAvailableException, IOException {
        try {
            BodyAdapter rbody = (BodyAdapter) bodies.get(id);
            if (rbody != null) {
                return rbody.getPolicy(securityContext);
            } else {
                throw new IOException("No BodyAdapter associated to id=" + id);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    public PublicKey getPublicKey(UniqueID id)
        throws SecurityNotAvailableException, IOException {
        try {
            BodyAdapter rbody = (BodyAdapter) bodies.get(id);
            if (rbody != null) {
                return rbody.getPublicKey();
            } else {
                throw new IOException("No BodyAdapter associated to id=" + id);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    public BodyAdapter getRemoteAdapter(UniqueID id) {
        try {
            BodyAdapter rbody = (BodyAdapter) bodies.get(id);
            ProActiveRuntimeForwarderImpl partf = (ProActiveRuntimeForwarderImpl) ProActiveRuntimeImpl.getProActiveRuntime();
            return new BodyAdapterForwarder(partf.getBodyAdapterForwarder(),
                rbody, id);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Oops in BodyForwarderImpl.getRemoteAdapter");
            return null;
        }
    }

    public byte[][] publicKeyExchange(UniqueID id, long sessionID,
        byte[] my_pub, byte[] my_cert, byte[] sig_code)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            KeyExchangeException, IOException {
        try {
            BodyAdapter rbody = (BodyAdapter) bodies.get(id);
            if (rbody != null) {
                return rbody.publicKeyExchange(sessionID, my_pub, my_cert,
                    sig_code);
            } else {
                throw new IOException("No BodyAdapter associated to id=" + id);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    public int receiveFTMessage(UniqueID id, FTMessage ev)
        throws IOException {
        try {
            BodyAdapter rbody = (BodyAdapter) bodies.get(id);
            if (rbody != null) {
                return rbody.receiveFTMessage(ev);
            } else {
                throw new IOException("No BodyAdapter associated to id=" + id);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    public int receiveReply(UniqueID id, Reply r) throws IOException {
        try {
            BodyAdapter rbody = (BodyAdapter) bodies.get(id);
            if (rbody != null) {
                return rbody.receiveReply(r);
            } else {
                throw new IOException("No BodyAdapter associated to id=" + id +
                    " request=" + r);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    public int receiveRequest(UniqueID id, Request request)
        throws IOException, RenegotiateSessionException {
        try {
            Object o = bodies.get(id);
            BodyAdapter rbody = (BodyAdapter) o;
            if (rbody != null) {
                return rbody.receiveRequest(request);
            } else {
                throw new IOException("No BodyAdapter associated to id=" + id);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    public void removeImmediateService(UniqueID id, String methodName,
        Class[] parametersTypes) throws IOException {
        try {
            BodyAdapter rbody = (BodyAdapter) bodies.get(id);
            if (rbody != null) {
                rbody.removeImmediateService(methodName, parametersTypes);
            } else {
                throw new IOException("No BodyAdapter associated to id=" + id);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    public byte[][] secretKeyExchange(UniqueID id, long sessionID, byte[] tmp,
        byte[] tmp1, byte[] tmp2, byte[] tmp3, byte[] tmp4)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            IOException {
        try {
            BodyAdapter rbody = (BodyAdapter) bodies.get(id);
            if (rbody != null) {
                return rbody.secretKeyExchange(sessionID, tmp, tmp1, tmp2,
                    tmp3, tmp4);
            } else {
                throw new IOException("No BodyAdapter associated to id=" + id);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    public void setImmediateService(UniqueID id, String methodName,
        Class[] parametersTypes) throws IOException {
        try {
            BodyAdapter rbody = (BodyAdapter) bodies.get(id);
            if (rbody != null) {
                rbody.setImmediateService(methodName, parametersTypes);
            } else {
                throw new IOException("No BodyAdapter associated to id=" + id);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    public void setImmediateService(UniqueID id, String methodName)
        throws IOException {
        try {
            BodyAdapter rbody = (BodyAdapter) bodies.get(id);
            if (rbody != null) {
                rbody.setImmediateService(methodName);
            } else {
                throw new IOException("No BodyAdapter associated to id=" + id);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    public long startNewSession(UniqueID id, Communication policy)
        throws SecurityNotAvailableException, IOException, 
            RenegotiateSessionException {
        try {
            BodyAdapter rbody = (BodyAdapter) bodies.get(id);
            if (rbody != null) {
                return rbody.startNewSession(policy);
            } else {
                throw new IOException("No BodyAdapter associated to id=" + id);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    public void terminate(UniqueID id) throws IOException {
        try {
            BodyAdapter rbody = (BodyAdapter) bodies.get(id);
            if (rbody != null) {
                // TODO probably remove this AO from our cache
                rbody.terminate();
            } else {
                throw new IOException("No BodyAdapter associated to id=" + id);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    public void terminateSession(UniqueID id, long sessionID)
        throws IOException, SecurityNotAvailableException {
        try {
            BodyAdapter rbody = (BodyAdapter) bodies.get(id);
            if (rbody != null) {
                rbody.terminateSession(sessionID);
            } else {
                throw new IOException("No BodyAdapter associated to id=" + id);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    public byte[] randomValue(UniqueID id, long sessionID, byte[] cl_rand)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            IOException {
        try {
            BodyAdapter rbody = (BodyAdapter) bodies.get(id);
            if (rbody != null) {
                return rbody.randomValue(sessionID, cl_rand);
            } else {
                throw new IOException("No BodyAdapter associated to id=" + id);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    public void updateLocation(UniqueID id, UniqueID uid, UniversalBody body)
        throws IOException {
        try {
            BodyAdapter rbody = (BodyAdapter) bodies.get(id);
            if (rbody != null) {
                rbody.updateLocation(uid, body);
            } else {
                throw new IOException("No BodyAdapter associated to id=" + id);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    public String getJobID(UniqueID id) throws IOException {
        try {
            BodyAdapter rbody = (BodyAdapter) bodies.get(id);
            if (rbody != null) {
                return rbody.getJobID();
            } else {
                throw new IOException("No BodyAdapter associated to id=" + id);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    public void addNFEListener(UniqueID id, NFEListener listener)
        throws IOException {
        try {
            BodyAdapter rbody = (BodyAdapter) bodies.get(id);
            if (rbody != null) {
                rbody.addNFEListener(listener);
            } else {
                throw new IOException("No BodyAdapter associated to id=" + id);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    public void changeProxiedBody(UniqueID id, Body newBody)
        throws IOException {
        try {
            BodyAdapter rbody = (BodyAdapter) bodies.get(id);
            if (rbody != null) {
                rbody.changeProxiedBody(newBody);
            } else {
                throw new IOException("No BodyAdapter associated to id=" + id);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    public int fireNFE(UniqueID id, NonFunctionalException e)
        throws IOException {
        try {
            BodyAdapter rbody = (BodyAdapter) bodies.get(id);
            if (rbody != null) {
                return rbody.fireNFE(e);
            } else {
                throw new IOException("No BodyAdapter associated to id=" + id);
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    public UniversalBody lookup(UniqueID id, String url)
        throws IOException {
        try {
            BodyAdapter rbody = (BodyAdapter) bodies.get(id);
            if (rbody != null) {
                return rbody.lookup(url);
            } else {
                throw new IOException("No BodyAdapter associated to id=" + id);
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    public void register(UniqueID id, String url) throws IOException {
        try {
            BodyAdapter rbody = (BodyAdapter) bodies.get(id);
            if (rbody != null) {
                rbody.register(url);
            } else {
                throw new IOException("No BodyAdapter associated to id=" + id);
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    public void removeNFEListener(UniqueID id, NFEListener listener)
        throws IOException {
        try {
            BodyAdapter rbody = (BodyAdapter) bodies.get(id);
            if (rbody != null) {
                rbody.removeNFEListener(listener);
            } else {
                throw new IOException("No BodyAdapter associated to id=" + id);
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }

    public void unregister(UniqueID id, String url) throws IOException {
        try {
            BodyAdapter rbody = (BodyAdapter) bodies.get(id);
            if (rbody != null) {
                rbody.unregister(url);
            } else {
                throw new IOException("No BodyAdapter associated to id=" + id);
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new IOException("No BodyAdapter associated to id=" + id);
        }
    }
}
