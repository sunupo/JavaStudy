.class Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer$Inner;
.super Ljava/lang/Object;
.source "Outer.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = "Inner"
.end annotation


# instance fields
.field inner_i:I

.field j:I

.field final synthetic this$0:Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;


# direct methods
.method constructor <init>(Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;)V
    .registers 3
    .param p1, "this$0"    # Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;

    .prologue
    .line 16
    iput-object p1, p0, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer$Inner;->this$0:Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 18
    const/16 v0, 0x64

    iput v0, p0, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer$Inner;->j:I

    .line 19
    const/4 v0, 0x1

    iput v0, p0, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer$Inner;->inner_i:I

    return-void
.end method


# virtual methods
.method inner_f1()V
    .registers 3

    .prologue
    .line 22
    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    invoke-static {}, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;->access$000()I

    move-result v1

    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    .line 24
    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    iget v1, p0, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer$Inner;->j:I

    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    .line 26
    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    iget v1, p0, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer$Inner;->j:I

    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    .line 28
    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    iget-object v1, p0, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer$Inner;->this$0:Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;

    invoke-static {v1}, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;->access$100(Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;)I

    move-result v1

    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    .line 30
    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    iget-object v1, p0, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer$Inner;->this$0:Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;

    invoke-static {v1}, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;->access$200(Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;)I

    move-result v1

    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    .line 31
    invoke-static {}, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;->outer_f1()V

    .line 32
    iget-object v0, p0, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer$Inner;->this$0:Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;

    invoke-virtual {v0}, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;->outer_f2()V

    .line 33
    return-void
.end method
