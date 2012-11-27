#!/usr/bin/env python

""" OpenImage iconvert module """
import os

from blueprint.layer import Layer
from blueprint.layer import Task

import blueprint.conf as conf

class IConvert(Layer):
    """ Layer to batch iconvert
    """
    def __init__(self, name, **kwargs):
        super(IConvert, self).__init__(name, **kwargs)
        self.requireArg("image_input", (str,))
        self.requireArg("image_output", (str,))
        self.requireArg("start", (int,))
        self.requireArg("end", (int,))

        self.addInput("image_input", self.getArg("image_input"))
        self.addInput("image_output", self.getArg("image_output"))


    def _execute(self, frames):
        """ Execute frames """

        start = self.getArg("start")
        end = self.getArg("end")
        i_input = str(self.getInput("image_input").path)
        i_output = str(self.getInput("image_output").path)
        convert_cmd = self.build_command()

        # for every start/end
        for f in xrange(start, end+1):
            cmd = list(convert_cmd)
            cmd.append(i_input % f)
            cmd.append(i_output % f)

            print cmd
            self.system(cmd)

        return

    def build_command(self):
        """ Build iconvert command options """
        cmd = [conf.get("Iconvert", "bin")]

        supported_options = ["v", "threads", "d", "g", "tile", "scanline", "compression",
                             "quality", "no_copy_image", "adjust_time", "caption",
                             "keyword", "clear_keywords", "attrib", "orientation",
                             "rotcw", "rotccw", "rot180", "inplace", "sRGB", "separate",
                             "contig", "no_clobber"]

        options_with_value = ["threads", "d", "g", "tile", "compression",
                              "quality", "caption", "keyword", "attrib",
                              "orientation"]

        for opt in supported_options:
            if self.isArgSet(opt):
                if len(opt) == 1:
                    iopt = "-%s" % opt
                else:
                    iopt = "--%s" % opt

                cmd.append(iopt)
                if opt in options_with_value:
                    value = self.getArg(opt)
                    if isinstance(value, tuple):
                        cmd.append(value[0])
                        cmd.append(value[1])
                    else:
                        cmd.append(value)

        return cmd



if __name__ == "__main__":
    pass

