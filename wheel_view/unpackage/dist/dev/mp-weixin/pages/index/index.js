"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      // 🎯 奖品列表（可自定义）
      prizes: [],
      // 🎨 每个扇形的颜色（循环使用）
      colors: [],
      // 📐 Canvas 相关
      canvasPixelSize: 300,
      // Canvas 宽高（像素）
      ctx: null,
      // Canvas 绘图上下文
      totalRotation: 0,
      // 当前累积旋转角度（用于控制转盘视觉旋转）
      isSpinning: false,
      // 是否正在旋转
      animationId: null,
      // requestAnimationFrame ID
      //结果文本
      resultText: ""
    };
  },
  mounted() {
    this.loadWheelConfig();
    this.$nextTick(() => {
      this.initCanvas();
    });
  },
  beforeDestroy() {
    if (this.animationId) {
      cancelAnimationFrame(this.animationId);
    }
  },
  methods: {
    async loadWheelConfig() {
      try {
        const res = await common_vendor.index.request({
          url: "http://localhost:8080/api/wheel/getPrize",
          method: "GET"
        });
        common_vendor.index.__f__("log", "at pages/index/index.vue:66", res);
        if (res.statusCode === 200 && res.data) {
          const prize = res.data;
          this.prizes = prize.map((item) => item.name);
          this.colors = prize.map((item) => item.color);
          common_vendor.index.__f__("log", "at pages/index/index.vue:71", this.prizes);
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/index/index.vue:74", "数据加载失败", e);
        common_vendor.index.showToast({
          title: "数据加载失败",
          icon: "none"
        });
      }
    },
    // 🛠 初始化 Canvas，获取绘图上下文并绘制转盘
    initCanvas() {
      this.ctx = common_vendor.index.createCanvasContext("wheelCanvas", this);
      this.drawWheel();
    },
    // 🎨 绘制转盘：扇形 + 文字
    drawWheel() {
      if (!this.ctx)
        return;
      const centerX = this.canvasPixelSize / 2;
      const centerY = this.canvasPixelSize / 2;
      const radius = this.canvasPixelSize / 2 - 15;
      const prizeCount = this.prizes.length;
      common_vendor.index.__f__("log", "at pages/index/index.vue:95", this.prizes);
      const anglePerPrize = 2 * Math.PI / prizeCount;
      for (let i = 0; i < prizeCount; i++) {
        const baseOffset = -Math.PI / 2;
        const startAngle = i * anglePerPrize + this.totalRotation * Math.PI / 180 + baseOffset;
        const endAngle = startAngle + anglePerPrize;
        this.ctx.beginPath();
        this.ctx.moveTo(centerX, centerY);
        this.ctx.arc(centerX, centerY, radius, startAngle, endAngle);
        this.ctx.closePath();
        this.ctx.fillStyle = this.colors[i % this.colors.length];
        this.ctx.fill();
        this.ctx.save();
        this.ctx.translate(centerX, centerY);
        this.ctx.rotate(startAngle + anglePerPrize / 2);
        this.ctx.textAlign = "center";
        this.ctx.fillStyle = "#ffffff";
        this.ctx.font = "20px sans-serif";
        this.ctx.fillText(this.prizes[i], radius * 0.7, 4);
        this.ctx.restore();
      }
      this.ctx.draw();
    },
    // 🎯 开始转盘旋转
    startSpin() {
      if (this.isSpinning)
        return;
      this.isSpinning = true;
      this.resultText = "";
      const prizeCount = this.prizes.length;
      const anglePerPrize = 360 / prizeCount;
      const selectedIndex = Math.floor(Math.random() * prizeCount);
      const targetAngle = selectedIndex * anglePerPrize + anglePerPrize / 2;
      const spins = 1;
      const finalAngle = spins * 360 + (360 - targetAngle);
      this.doRotationAnimation(finalAngle, () => {
        this.resultText = `😀今天中午吃：${this.prizes[selectedIndex]}`;
        this.isSpinning = false;
      });
    },
    // 🌀 执行旋转动画（使用 requestAnimationFrame）
    doRotationAnimation(targetTotalAngle, onFinish) {
      const totalTarget = targetTotalAngle;
      const duration = 4e3;
      const startTime = Date.now();
      const step = () => {
        const elapsed = Date.now() - startTime;
        let progress = Math.min(elapsed / duration, 1);
        progress = 1 - Math.pow(1 - progress, 3);
        const currentAngle = totalTarget * progress;
        this.totalRotation = currentAngle;
        this.drawWheel();
        if (progress < 1) {
          this.animationId = requestAnimationFrame(step);
        } else {
          this.totalRotation = totalTarget;
          this.drawWheel();
          if (onFinish)
            onFinish();
        }
      };
      step();
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: $data.canvasPixelSize + "px",
    b: $data.canvasPixelSize + "px",
    c: common_vendor.t($data.isSpinning ? "转动中..." : "开始转动"),
    d: common_vendor.o((...args) => $options.startSpin && $options.startSpin(...args)),
    e: $data.isSpinning,
    f: $data.resultText
  }, $data.resultText ? {
    g: common_vendor.t($data.resultText)
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-1cf27b2a"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/index/index.js.map
