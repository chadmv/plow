#include "wrangleplow.h"
#include <QApplication>

int main(int argc, char *argv[])
{
  QApplication app(argc, argv);

  Plow::WranglePlow::MainWindow wrangleplow;
  wrangleplow.show();
  wrangleplow.updateJobs();

  return app.exec();
}