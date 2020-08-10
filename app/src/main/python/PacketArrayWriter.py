from com.bedrankarakoc.mobilesentinel import LogPacket
from parsers.qualcomm.pycrate.pycrate_asn1dir import RRCLTE

import json
from binascii import unhexlify, hexlify

class PacketArrayWriter:

    def __init__(self, packet_list):
        self.packet_list = packet_list

    #TODO: Add support for all subtypes
    def append_packet(self, rrc_subtype, msg):

        if str(rrc_subtype) == "gsmtap_lte_rrc_types.DL_DCCH":
            sch = RRCLTE.EUTRA_RRC_Definitions.DL_DCCH_Message
            sch.from_uper(unhexlify(msg))
            json_string = sch.to_json()
            data = json.loads(json_string)

            msg_name = ""
            for iter in data['message']['c1']:
                msg_name = str(iter)
            if msg_name is not None:
                log_pkt = LogPacket(msg_name, str(json_string))
            else:
                log_pkt = LogPacket(str(rrc_subtype), str(json_string))

            self.packet_list.add(log_pkt)





