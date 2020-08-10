from binascii import hexlify, unhexlify
from parsers.qualcomm.pycrate.pycrate_asn1dir import RRCLTE

import json
import string


class DetectionHandler:

    def __init__(self):
        self.initial_drb_id = 0
        self.drb_id = 0
        self.call_flow = []

    def filter_packet(self, rrc_subtype, msg):
        # Filter for rrcConnectionReconfiguration
        if str(rrc_subtype) == "gsmtap_lte_rrc_types.DL_DCCH":

            sch = RRCLTE.EUTRA_RRC_Definitions.DL_DCCH_Message
            sch.from_uper(unhexlify(msg))
            json_string = sch.to_json()
            data = json.loads(json_string)
            if 'rrcConnectionReconfiguration' in data['message']['c1']:
                return data

    def revolte_check(self, rrc_subtype, msg):

        parsed_msg = self.filter_packet(rrc_subtype, msg)
        if parsed_msg is not None:

            try:
                if 'radioResourceConfigDedicated' in \
                        parsed_msg['message']['c1']['rrcConnectionReconfiguration'][
                            'criticalExtensions'][
                            'c1']['rrcConnectionReconfiguration-r8']:
                    if 'drb-ToReleaseList' in \
                            parsed_msg['message']['c1']['rrcConnectionReconfiguration'][
                                'criticalExtensions'][
                                'c1']['rrcConnectionReconfiguration-r8'][
                                'radioResourceConfigDedicated']:
                        for iter in \
                                parsed_msg['message']['c1']['rrcConnectionReconfiguration'][
                                    'criticalExtensions']['c1'][
                                    'rrcConnectionReconfiguration-r8'][
                                    'radioResourceConfigDedicated'][
                                    'drb-ToReleaseList']:

                            if len(self.call_flow) != 0:
                                # If DRB ID is reused return True
                                if iter in self.call_flow:
                                    return True

                            # Add each DRB ID to the list
                            self.call_flow.append(iter)
                            print(self.call_flow)


            except KeyError as e:
                print(e)

            try:
                if 'securityConfigHO' in \
                        parsed_msg['message']['c1']['rrcConnectionReconfiguration'][
                            'criticalExtensions'][
                            'c1']['rrcConnectionReconfiguration-r8']:

                    if 'handoverType' in \
                            parsed_msg['message']['c1']['rrcConnectionReconfiguration'][
                                'criticalExtensions'][
                                'c1']['rrcConnectionReconfiguration-r8']['securityConfigHO']:

                        for iter in \
                                parsed_msg['message']['c1']['rrcConnectionReconfiguration'][
                                    'criticalExtensions']['c1'][
                                    'rrcConnectionReconfiguration-r8']['securityConfigHO'][
                                    'handoverType']:
                            print("HandoverType")

                            print(iter)
                            print("Calling clear")
                            # Clear list after a securityConfigHO trigger as old DRB IDs may be reused now
                            self.call_flow.clear()



            except KeyError as e:
                print(e)
