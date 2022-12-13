.class public Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;
.super Ljava/lang/Object;
.source "Outer.java"


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer$Inner;
    }
.end annotation


# static fields
.field private static i:I


# instance fields
.field private j:I

.field private k:I


# direct methods
.method static constructor <clinit>()V
    .registers 1

    .prologue
    .line 4
    const/4 v0, 0x1

    sput v0, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;->i:I

    return-void
.end method

.method public constructor <init>()V
    .registers 2

    .prologue
    .line 3
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 5
    const/16 v0, 0xa

    iput v0, p0, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;->j:I

    .line 6
    const/16 v0, 0x14

    iput v0, p0, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;->k:I

    return-void
.end method

.method static synthetic access$000()I
    .registers 1

    .prologue
    .line 3
    sget v0, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;->i:I

    return v0
.end method

.method static synthetic access$100(Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;)I
    .registers 2
    .param p0, "x0"    # Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;

    .prologue
    .line 3
    iget v0, p0, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;->j:I

    return v0
.end method

.method static synthetic access$200(Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;)I
    .registers 2
    .param p0, "x0"    # Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;

    .prologue
    .line 3
    iget v0, p0, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;->k:I

    return v0
.end method

.method public static main([Ljava/lang/String;)V
    .registers 3
    .param p0, "args"    # [Ljava/lang/String;

    .prologue
    .line 61
    new-instance v0, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;

    invoke-direct {v0}, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;-><init>()V

    .line 62
    .local v0, "out":Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;
    new-instance v1, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer$Inner;

    invoke-virtual {v0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    invoke-direct {v1, v0}, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer$Inner;-><init>(Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;)V

    .line 63
    .local v1, "outin":Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer$Inner;
    invoke-virtual {v1}, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer$Inner;->inner_f1()V

    .line 64
    return-void
.end method

.method public static outer_f1()V
    .registers 0

    .prologue
    .line 9
    return-void
.end method

.method public static outer_f4()V
    .registers 2

    .prologue
    .line 45
    new-instance v1, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;

    invoke-direct {v1}, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;-><init>()V

    .line 47
    .local v1, "out":Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;
    new-instance v0, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer$Inner;

    invoke-virtual {v1}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    invoke-direct {v0, v1}, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer$Inner;-><init>(Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;)V

    .line 49
    .local v0, "inner":Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer$Inner;
    invoke-virtual {v0}, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer$Inner;->inner_f1()V

    .line 50
    return-void
.end method


# virtual methods
.method public outer_f2()V
    .registers 1

    .prologue
    .line 12
    return-void
.end method

.method public outer_f3()V
    .registers 2

    .prologue
    .line 38
    new-instance v0, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer$Inner;

    invoke-direct {v0, p0}, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer$Inner;-><init>(Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer;)V

    .line 39
    .local v0, "inner":Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer$Inner;
    invoke-virtual {v0}, Lcom/java/sjq/base/innerClass/memberInnerCLass/Outer$Inner;->inner_f1()V

    .line 40
    return-void
.end method
