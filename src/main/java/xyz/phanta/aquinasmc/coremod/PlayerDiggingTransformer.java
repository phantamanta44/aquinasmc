package xyz.phanta.aquinasmc.coremod;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;

public class PlayerDiggingTransformer implements IClassTransformer {

    private static final String CLNAME_EPLAYER = "net/minecraft/entity/player/EntityPlayer";
    private static final String CLNAME_EPMP = "net/minecraft/entity/player/EntityPlayerMP";
    private static final String CLNAME_PPD = "net/minecraft/network/play/client/CPacketPlayerDigging";

    private static final String CLNAME_EVBUS = "net/minecraftforge/fml/common/eventhandler/EventBus";
    private static final String MNAME_EVBUS_POST = "post";
    private static final String MDESC_EVBUS_POST = "(Lnet/minecraftforge/fml/common/eventhandler/Event;)Z";

    private static final String CLNAME_MCF = "net/minecraftforge/common/MinecraftForge";
    private static final String FNAME_MCF_EVBUS = "EVENT_BUS";
    private static final String FDESC_MCF_EVBUS = "L" + CLNAME_EVBUS + ";";

    private static final String CLNAME_NHPS = "net/minecraft/network/NetHandlerPlayServer";
    private static final String FNAME_NHPS_PLAYER = "player";
    private static final String FDESC_NHPS_PLAYER = "L" + CLNAME_EPMP + ";";

    private static final String CLNAME_PDE = "xyz/phanta/aquinasmc/event/PlayerDiggingEvent";
    private static final String MDESC_PDE_INIT = String.format("(L%s;L%s;)V", CLNAME_EPLAYER, CLNAME_PPD);

    @Override
    public byte[] transform(String name, String transformedName, byte[] code) {
        if (transformedName.equals("net.minecraft.network.NetHandlerPlayServer")) {
            ClassReader reader = new ClassReader(code);
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
            reader.accept(new ClassVisitorNetHandlerPlayServer(writer), 0);
            return writer.toByteArray();
        }
        return code;
    }

    private static void injectCode(MethodVisitor writer) {
        writer.visitFieldInsn(Opcodes.GETSTATIC, CLNAME_MCF, FNAME_MCF_EVBUS, FDESC_MCF_EVBUS);
        writer.visitTypeInsn(Opcodes.NEW, CLNAME_PDE);
        writer.visitInsn(Opcodes.DUP);
        writer.visitVarInsn(Opcodes.ALOAD, 0);
        writer.visitFieldInsn(Opcodes.GETFIELD, CLNAME_NHPS, FNAME_NHPS_PLAYER, FDESC_NHPS_PLAYER);
        writer.visitVarInsn(Opcodes.ALOAD, 1);
        writer.visitMethodInsn(Opcodes.INVOKESPECIAL, CLNAME_PDE, "<init>", MDESC_PDE_INIT, false);
        writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, CLNAME_EVBUS, MNAME_EVBUS_POST, MDESC_EVBUS_POST, false);
        Label notCancelled = new Label();
        writer.visitJumpInsn(Opcodes.IFEQ, notCancelled);
        writer.visitInsn(Opcodes.RETURN);
        writer.visitLabel(notCancelled);
        System.out.println("Successfully injected player digging event!");
    }

    private static class ClassVisitorNetHandlerPlayServer extends ClassVisitor {

        private boolean done = false;

        ClassVisitorNetHandlerPlayServer(ClassVisitor cv) {
            super(Opcodes.ASM5, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (!done && (name.equals("processPlayerDigging") || name.equals("func_147345_a"))) {
                done = true;
                return new MethodVisitorProcessPlayerDigging(super.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

    }

    private static class MethodVisitorProcessPlayerDigging extends MethodVisitor {

        private boolean done = false;

        MethodVisitorProcessPlayerDigging(MethodVisitor mv) {
            super(Opcodes.ASM5, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            if (!done && opcode == Opcodes.INVOKEVIRTUAL && (name.equals("markPlayerActive") || name.equals("func_143004_u"))) {
                injectCode(mv);
                done = true;
            }
        }

    }

}
