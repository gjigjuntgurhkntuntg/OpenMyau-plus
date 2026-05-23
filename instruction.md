# Instructions

<You are refactoring a Minecraft 1.8.9 Scaffold module to behave closer to vanilla client behavior against modern prediction-based anticheats like GrimAC.

Important:
Do NOT implement exploit logic or packet abuse.
The objective is accurate vanilla synchronization, realistic placement timing, and proper client/server state consistency.

Use this GrimAC AirLiquidPlace source behavior as the reference model:

Grim validates whether the support block actually exists server-side during placement.
Grim tracks recent block modifications and packet chronology.
Grim detects placements against air/liquids caused by invalid timing or prediction mismatch.
Grim heavily relies on realistic ordering of movement, digging, and placement packets.

Refactor the scaffold implementation with these goals:

Core Fixes
1. Placement Synchronization

Ensure block placement only occurs after:

movement packets containing updated rotations are sent
valid support block existence is confirmed
client-side prediction matches likely server state

Never place blocks using future predicted support positions.

2. Support Block Validation

Before placement:

validate the target support block is non-air
validate the support block is not a liquid
validate block face legality
validate raytrace hit result

Implement a dedicated PlacementValidator.

3. Packet Order Consistency

Match vanilla ordering as closely as possible:

rotation update
movement packet
held item update (if needed)
placement packet

Avoid:

instant place after silent rotation
async inventory spoofing
invalid pre-flying placement order
4. Rotation Consistency

Ensure:

rotations are smooth and believable
server-facing rotations match placement vectors
no impossible snap rotations
hit vectors align with actual visible placement face

Create:

RotationManager
RotationSmoother
RaytraceUtil
5. Movement Consistency

Prevent simulation mismatches:

keep SafeWalk logic vanilla-like
avoid invalid edge motion
avoid timer manipulation
avoid impossible Y motion
avoid overaggressive expand logic

Movement prediction should stay synchronized with vanilla physics.

6. Cursor / HitVec Realism

Do not use static cursor vectors.

Generate believable placement hit vectors:

based on actual raytrace collision
slightly varied within legal bounds
consistent with player rotation

Avoid repeated perfect cursor patterns like:

0.8125
0.1875
0.0625
7. Tick Synchronization

Ensure placement logic respects:

tick order
block update timing
same-tick break/place edge cases

Do not assume async world state is immediately correct.

8. Debugging Tools

Add optional debug overlay/logging:

packet order
support block state
rotation before place
raytrace target
cursor vector
placement delay
simulation delta
current scaffold target block
Architecture Requirements

Refactor into:

ScaffoldModule
RotationManager
PlacementManager
PlacementValidator
RaytraceUtil
MovementPrediction
PacketOrderController
HitVectorGenerator

Avoid giant monolithic Scaffold.java implementations.

Goal

The final scaffold should resemble legitimate vanilla player behavior and remain stable under Grim-style world consistency checks instead of relying on exploit-style bypass logic.>
