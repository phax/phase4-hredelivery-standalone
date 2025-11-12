/*
 * Copyright (C) 2023-2025 Philip Helger (www.helger.com)
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
package com.helger.phase4.hredeliverystandalone.controller;

import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.helger.base.io.nonblocking.NonBlockingByteArrayInputStream;
import com.helger.base.string.StringHelper;
import com.helger.hredelivery.commons.EHREDeliverySML;
import com.helger.hredelivery.commons.sbdh.HREDeliverySBDHData;
import com.helger.hredelivery.commons.sbdh.HREDeliverySBDHDataReadException;
import com.helger.hredelivery.commons.sbdh.HREDeliverySBDHDataReader;
import com.helger.hredelivery.commons.security.HREDeliveryTrustedCA;
import com.helger.peppol.servicedomain.EPeppolNetwork;
import com.helger.peppolid.factory.PeppolIdentifierFactory;
import com.helger.phase4.hredelivery.Phase4HREdeliverySendingReport;
import com.helger.phase4.hredeliverystandalone.APConfig;
import com.helger.phase4.logging.Phase4LoggerFactory;
import com.helger.security.certificate.TrustedCAChecker;

/**
 * This is the primary REST controller for the APIs to send messages over Peppol.
 *
 * @author Philip Helger
 */
@RestController
public class HREDeliverySenderController
{
  static final String HEADER_X_TOKEN = "X-Token";
  private static final Logger LOGGER = Phase4LoggerFactory.getLogger (HREDeliverySenderController.class);

  @PostMapping (path = "/sendas4/{senderId}/{receiverId}/{docTypeId}/{processId}",
                produces = MediaType.APPLICATION_JSON_VALUE)
  public String sendPeppolMessage (@RequestHeader (name = HEADER_X_TOKEN, required = true) final String xtoken,
                                   @RequestBody final byte [] aPayloadBytes,
                                   @PathVariable final String senderId,
                                   @PathVariable final String receiverId,
                                   @PathVariable final String docTypeId,
                                   @PathVariable final String processId)
  {
    if (!APConfig.isSendingEnabled ())
    {
      LOGGER.info ("Peppol AP sending is disabled");
      throw new HttpNotFoundException ();
    }

    if (StringHelper.isEmpty (xtoken))
    {
      LOGGER.error ("The specific token header is missing");
      throw new HttpForbiddenException ();
    }
    if (!xtoken.equals (APConfig.getPhase4ApiRequiredToken ()))
    {
      LOGGER.error ("The specified token value does not match the configured required token");
      throw new HttpForbiddenException ();
    }

    final EPeppolNetwork eStage = APConfig.getPeppolStage ();
    final EHREDeliverySML eSML = eStage.isProduction () ? EHREDeliverySML.PRODUCTION : EHREDeliverySML.DEMO;
    final TrustedCAChecker aAPCA = eStage.isProduction () ? HREDeliveryTrustedCA.hrEdeliveryFinaProduction ()
                                                          : HREDeliveryTrustedCA.hrEdeliveryFinaDemo ();
    LOGGER.info ("Trying to send HR eDelivery " +
                 eStage.name () +
                 " message from '" +
                 senderId +
                 "' to '" +
                 receiverId +
                 "' using '" +
                 docTypeId +
                 "' and '" +
                 processId +
                 "'");
    final Phase4HREdeliverySendingReport aSendingReport = HREDeliverySender.sendHREDeliveryMessageCreatingSbdh (eSML,
                                                                                                                aAPCA,
                                                                                                                aPayloadBytes,
                                                                                                                senderId,
                                                                                                                receiverId,
                                                                                                                docTypeId,
                                                                                                                processId);

    // Return as JSON
    return aSendingReport.getAsJsonString ();
  }

  @PostMapping (path = "/sendsbdh/{docTypeId}/{processId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public String sendPeppolSbdhMessage (@RequestHeader (name = HEADER_X_TOKEN, required = true) final String xtoken,
                                       @RequestBody final byte [] aPayloadBytes,
                                       @PathVariable final String docTypeId,
                                       @PathVariable final String processId)
  {
    if (!APConfig.isSendingEnabled ())
    {
      LOGGER.info ("Peppol AP sending is disabled");
      throw new HttpNotFoundException ();
    }

    if (StringHelper.isEmpty (xtoken))
    {
      LOGGER.error ("The specific token header is missing");
      throw new HttpForbiddenException ();
    }
    if (!xtoken.equals (APConfig.getPhase4ApiRequiredToken ()))
    {
      LOGGER.error ("The specified token value does not match the configured required token");
      throw new HttpForbiddenException ();
    }

    final EPeppolNetwork eStage = APConfig.getPeppolStage ();
    final EHREDeliverySML eSML = eStage.isProduction () ? EHREDeliverySML.PRODUCTION : EHREDeliverySML.DEMO;
    final TrustedCAChecker aAPCA = eStage.isProduction () ? HREDeliveryTrustedCA.hrEdeliveryFinaProduction ()
                                                          : HREDeliveryTrustedCA.hrEdeliveryFinaDemo ();
    final Phase4HREdeliverySendingReport aSendingReport = new Phase4HREdeliverySendingReport (eSML);

    final HREDeliverySBDHData aData;
    try
    {
      aData = new HREDeliverySBDHDataReader (PeppolIdentifierFactory.INSTANCE).extractData (new NonBlockingByteArrayInputStream (aPayloadBytes));
    }
    catch (final HREDeliverySBDHDataReadException ex)
    {
      // TODO This error handling might be improved to return a status error
      // instead
      aSendingReport.setSBDHParseException (ex);
      aSendingReport.setSendingSuccess (false);
      aSendingReport.setOverallSuccess (false);
      return aSendingReport.getAsJsonString ();
    }

    aSendingReport.setSenderID (aData.getSenderAsIdentifier ());
    aSendingReport.setReceiverID (aData.getReceiverAsIdentifier ());
    aSendingReport.setSBDHInstanceIdentifier (aData.getInstanceIdentifier ());

    final String sSenderID = aData.getSenderAsIdentifier ().getURIEncoded ();
    final String sReceiverID = aData.getReceiverAsIdentifier ().getURIEncoded ();
    LOGGER.info ("Trying to send HR eDelivery " +
                 eStage.name () +
                 " SBDH message from '" +
                 sSenderID +
                 "' to '" +
                 sReceiverID +
                 "' using '" +
                 docTypeId +
                 "' and '" +
                 processId +
                 "'");

    HREDeliverySender.sendHREDeliveryMessagePredefinedSbdh (aData, eSML, aAPCA, docTypeId, processId, aSendingReport);

    // Return result JSON
    return aSendingReport.getAsJsonString ();
  }
}
