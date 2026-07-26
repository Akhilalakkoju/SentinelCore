import { useEffect, useState } from "react";
import {
  PieChart,
  Pie,
  Cell,
  ResponsiveContainer,
  Tooltip,
} from "recharts";
import { motion } from "framer-motion";
import api from "../services/api";
import CustomPieTooltip from "./CustomPieTooltip";

function SeverityChart() {
  const [data, setData] = useState([]);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const response = await api.get("/dashboard/chart");

      const formatted = response.data.map((item) => ({
        name: item.severity,
        value: item.count,
      }));

      setData(formatted);
    } catch (error) {
      console.log(error);
    }
  };

  const COLORS = [
    "#ef4444",
    "#f97316",
    "#eab308",
    "#22c55e",
  ];

  const totalThreats = data.reduce(
    (sum, item) => sum + item.value,
    0
  );

  return (
    <motion.div
      initial={{ opacity: 0, x: 40 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ duration: 0.6 }}
      className="
        h-full
        rounded-3xl
        bg-slate-900/80
        backdrop-blur-xl
        border border-slate-800
        shadow-2xl
        p-8
        flex
        flex-col
      "
    >
      {/* Header */}

      <div>

        <h2 className="text-3xl font-bold text-white">
          Severity Distribution
        </h2>

        <p className="text-slate-400 mt-2">
          Current threat severity overview
        </p>

      </div>

      {/* Donut Chart */}

      <div className="flex-1 flex justify-center items-center">

        <ResponsiveContainer
          width="100%"
          height={320}
        >

          <PieChart>

            <Pie
              data={data}
              dataKey="value"
              nameKey="name"
              cx="50%"
              cy="50%"
              innerRadius={60}
              outerRadius={95}
              paddingAngle={5}
              stroke="#ffffff"
              strokeWidth={2}
              animationDuration={1800}
            >

              {data.map((entry, index) => (

                <Cell
                  key={index}
                  fill={COLORS[index]}
                />

              ))}

            </Pie>

            <Tooltip content={<CustomPieTooltip />} />

            {/* Center Text */}

            <text
              x="50%"
              y="47%"
              textAnchor="middle"
              dominantBaseline="middle"
              fill="var(--sc-text-primary)"
              fontSize="34"
              fontWeight="bold"
              className="fill-slate-900 dark:fill-white font-bold"
            >
              {totalThreats}
            </text>

            <text
              x="50%"
              y="60%"
              textAnchor="middle"
              dominantBaseline="middle"
              fill="var(--sc-text-secondary)"
              fontSize="18"
              className="fill-slate-600 dark:fill-slate-400 font-semibold"
            >
              Threats
            </text>


          </PieChart>

        </ResponsiveContainer>

      </div>

    </motion.div>
  );
}

export default SeverityChart;