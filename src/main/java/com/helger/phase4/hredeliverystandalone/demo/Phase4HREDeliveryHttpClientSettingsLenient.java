package com.helger.phase4.hredeliverystandalone.demo;

import com.helger.phase4.profile.hredelivery.Phase4HREDeliveryHttpClientSettings;

/**
 * Lenient version of Phase4HREDeliveryHttpClientSettings primarily for Demo purposes. For
 * production it is recommended to use the base class.
 *
 * @author Philip Helger
 */
public class Phase4HREDeliveryHttpClientSettingsLenient extends Phase4HREDeliveryHttpClientSettings
{
  public Phase4HREDeliveryHttpClientSettingsLenient ()
  {
    // Disable server hostname check
    setHostnameVerifierVerifyAll ();
  }
}
