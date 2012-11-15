
import os
import json
import bpy

# If nodes are not enabled...we can enable them and use this to figure out outputs.
# bpy.context.scene.use_nodes = True
#[('Composite', bpy.data...nodes["Composite"]), ('Render Layers', bpy.data...nodes["Render Layers"])]

def getOutputFileNodes():
    result = []
    for ntype, item in bpy.context.scene.node_tree.nodes.items():
        if item.type == "OUTPUT_FILE":
            result.append(item)
    return result

def setup():
    result = { }

    for node in getOutputFileNodes():
        result[node.name] = [node.base_path, {
            "res_x": bpy.context.scene.render.resolution_x,
            "res_y": bpy.context.scene.render.resolution_y,
            "file_format": node.file_slots.items()[0][1].format.file_format,
            "color_depth": node.file_slots.items()[0][1].format.color_depth}
        ]

    path = os.environ["PLOW_BLENDER_SETUP_PATH"]
    json.dump(result, open(path, "w"))

setup();