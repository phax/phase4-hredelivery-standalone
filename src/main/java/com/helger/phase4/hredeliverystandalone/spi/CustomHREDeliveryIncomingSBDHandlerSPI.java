/*
 * Copyright (C) 2025 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.phase4.hredeliverystandalone.spi;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.unece.cefact.namespaces.sbdh.StandardBusinessDocument;

import com.helger.annotation.style.IsSPIImplementation;
import com.helger.collection.commons.ICommonsList;
import com.helger.hredelivery.commons.sbdh.HREDeliverySBDHData;
import com.helger.http.header.HttpHeaderMap;
import com.helger.peppolid.IDocumentTypeIdentifier;
import com.helger.peppolid.IProcessIdentifier;
import com.helger.phase4.ebms3header.Ebms3Error;
import com.helger.phase4.ebms3header.Ebms3UserMessage;
import com.helger.phase4.hredelivery.servlet.IPhase4HREDeliveryIncomingSBDHandlerSPI;
import com.helger.phase4.hredeliverystandalone.APConfig;
import com.helger.phase4.incoming.IAS4IncomingMessageMetadata;
import com.helger.phase4.incoming.IAS4IncomingMessageState;
import com.helger.phase4.logging.Phase4LoggerFactory;
import com.helger.phase4.util.Phase4Exception;
import com.helger.security.certificate.CertificateHelper;

/**
 * This is a way of handling incoming Peppol messages
 *
 * @author Philip Helger
 */
@IsSPIImplementation
public class CustomHREDeliveryIncomingSBDHandlerSPI implements IPhase4HREDeliveryIncomingSBDHandlerSPI
{
  private static final Logger LOGGER = Phase4LoggerFactory.getLogger (CustomHREDeliveryIncomingSBDHandlerSPI.class);

  public void handleIncomingSBD (@NonNull final IAS4IncomingMessageMetadata aMessageMetadata,
                                 @NonNull final HttpHeaderMap aHeaders,
                                 @NonNull final Ebms3UserMessage aUserMessage,
                                 @NonNull final byte [] aSBDBytes,
                                 @NonNull final StandardBusinessDocument aSBD,
                                 @NonNull final HREDeliverySBDHData aPeppolSBD,
                                 @NonNull final IDocumentTypeIdentifier aDocTypeID,
                                 @NonNull final IProcessIdentifier aProcessID,
                                 @NonNull final IAS4IncomingMessageState aIncomingState,
                                 @NonNull final ICommonsList <Ebms3Error> aProcessingErrorMessages) throws Exception
  {
    if (!APConfig.isReceivingEnabled ())
    {
      LOGGER.info ("Peppol AP receiving is disabled");
      throw new Phase4Exception ("Peppol AP receiving is disabled");
    }

    final String sMyPartyID = APConfig.getMyPartyID ();

    // Example code snippets how to get data
    LOGGER.info ("Received a new Peppol Message");
    LOGGER.info ("  C1 = " + aPeppolSBD.getSenderAsIdentifier ().getURIEncoded ());
    LOGGER.info ("  C2 = " + CertificateHelper.getSubjectCN (aIncomingState.getSigningCertificate ()));
    LOGGER.info ("  C3 = " + sMyPartyID);
    LOGGER.info ("  C4 = " + aPeppolSBD.getReceiverAsIdentifier ().getURIEncoded ());
    LOGGER.info ("  DocType = " + aDocTypeID.getURIEncoded ());
    LOGGER.info ("  Process = " + aProcessID.getURIEncoded ());

    // TODO add your code here
    // E.g. write to disk, write to S3, write to database, write to queue...
    LOGGER.error ("You need to implement handleIncomingSBD to deal with incoming messages");

    // In case there is an error, throw any Exception -> will lead to an AS4
    // Error Message to the sender
  }
}
