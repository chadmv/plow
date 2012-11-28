#include <QApplication>

#include "gui/common.h"
#include "wrangleplow.h"

int main(int argc, char *argv[])
{
  
  QApplication *app = Plow::Gui::createQApp(argc, argv);

  WranglePlow::MainWindow wrangleplow;
  wrangleplow.resize(1024, 768);
  wrangleplow.show();

  return app->exec();
}
