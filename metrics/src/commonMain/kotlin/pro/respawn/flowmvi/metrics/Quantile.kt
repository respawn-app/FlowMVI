package pro.respawn.flowmvi.metrics

internal enum class Quantile(val value: Double, val label: String) {
    Q50(0.5, "0.5"),
    Q90(0.9, "0.9"),
    Q95(0.95, "0.95"),
    Q99(0.99, "0.99"),
}
