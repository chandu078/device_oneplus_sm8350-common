/*
 * Copyright (C) 2022 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

#include <android-base/logging.h>
#include <android-base/properties.h>

#define _REALLY_INCLUDE_SYS__SYSTEM_PROPERTIES_H_
#include <sys/_system_properties.h>

using android::base::GetProperty;

/*
 * SetProperty does not allow updating read only properties and as a result
 * does not work for our use case. Write "OverrideProperty" to do practically
 * the same thing as "SetProperty" without this restriction.
 */
void property_override(char const prop[], char const value[]) {
  prop_info *pi;

  pi = (prop_info *)__system_property_find(prop);
  if (pi)
    __system_property_update(pi, value, strlen(value));
  else
    __system_property_add(prop, strlen(prop), value, strlen(value));
}

/*
 * Only for read-only properties. Properties that can be wrote to more
 * than once should be set in a typical init script (e.g. init.oplus.hw.rc)
 * after the original property has been set.
 */
void vendor_load_properties() {
  std::string project_codename = android::base::GetProperty("ro.boot.project_codename", "");
  int rf_version = stoi(android::base::GetProperty("ro.boot.rf_version", ""));
  if(project_codename == "lemonade"){
      /* OnePlus 9 */
      switch (rf_version){
        case 11:
          /* China */
          property_override("ro.product.model", "LE2110");
          break;
        case 13:
          /* India */
          property_override("ro.product.model", "LE2111");
          break;
        case 21:
          /* Europe */
          property_override("ro.product.model", "LE2113");
          break;
        case 22:
          /* Global / US Unlocked */
          property_override("ro.product.model", "LE2115");
          break;
        default:
          /* Generic */
          property_override("ro.product.model", "LE2115");
          break;
      }
  } else if(project_codename == "lemonadet"){
      /* OnePlus 9 T-Mobile */
      switch (rf_version){
        case 12:
          /* T-Mobile */
          property_override("ro.product.model", "LE2117");
          break;
        default:
          /* Generic */
          property_override("ro.product.model", "LE2115");
          break;
      }
  } else if(project_codename == "lemonadep"){
      /* OnePlus 9 Pro */
      switch (rf_version){
        case 11:
          /* China */
          property_override("ro.product.model", "LE2120");
          break;
        case 13:
          /* India */
          property_override("ro.product.model", "LE2121");
          break;
        case 21:
          /* Europe */
          property_override("ro.product.model", "LE2123");
          break;
        case 22:
          /* Global / US Unlocked */
          property_override("ro.product.model", "LE2125");
          break;
        default:
          /* Generic */
          property_override("ro.product.model", "LE2125");
          break;
      }
  } else if(project_codename == "lemonadept"){
      /* OnePlus 9 Pro T-Mobile */
      switch (rf_version){
        case 12:
          /* T-Mobile */
          property_override("ro.product.model", "LE2127");
          break;
        default:
          /* Generic */
          property_override("ro.product.model", "LE2125");
          break;
      }
   }
}
